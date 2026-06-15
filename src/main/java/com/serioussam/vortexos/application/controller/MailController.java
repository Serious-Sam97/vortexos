package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.MailRequest;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.application.websocket.ChatWebSocketHandler;
import com.serioussam.vortexos.domain.mail.Mail;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaMailRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * VortexMail — real mail between users. Every operation is scoped to the signed-in
 * user (CurrentUser); a message row is shared by sender and recipient with per-side
 * soft delete. New mail pings the recipient over the WebSocket (if connected).
 */
@RestController
@RequestMapping("/mail")
public class MailController {
    private final JpaMailRepository repo;
    private final JpaUserRepository users;
    private final CurrentUser currentUser;
    private final ChatWebSocketHandler ws;

    public MailController(JpaMailRepository repo, JpaUserRepository users, CurrentUser currentUser, ChatWebSocketHandler ws) {
        this.repo = repo;
        this.users = users;
        this.currentUser = currentUser;
        this.ws = ws;
    }

    private Map<String, Object> summary(Mail m, Long me) {
        return Map.of(
                "id", m.getId(),
                "fromName", m.getFromName() == null ? "" : m.getFromName(),
                "toName", m.getToName() == null ? "" : m.getToName(),
                "subject", m.getSubject() == null ? "" : m.getSubject(),
                "body", m.getBody() == null ? "" : m.getBody(),
                "createdAt", m.getCreatedAt(),
                "read", m.isReadByRecipient(),
                "draft", m.isDraft(),
                "incoming", m.getToId() != null && m.getToId().equals(me));
    }

    /** List a folder: inbox | sent | drafts | trash. */
    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(defaultValue = "inbox") String folder) {
        Long me = this.currentUser.id();
        List<Mail> mails = switch (folder) {
            case "sent" -> this.repo.findByFromIdAndDraftFalseAndTrashedBySenderFalseOrderByCreatedAtDesc(me);
            case "drafts" -> this.repo.findByFromIdAndDraftTrueOrderByCreatedAtDesc(me);
            case "trash" -> this.repo.findTrash(me);
            default -> this.repo.findByToIdAndDraftFalseAndTrashedByRecipientFalseOrderByCreatedAtDesc(me);
        };
        return mails.stream().map(m -> summary(m, me)).toList();
    }

    /** Unread inbox count (for the tray badge / notifications). */
    @GetMapping("/unread")
    public Map<String, Object> unread() {
        return Map.of("count", this.repo.countByToIdAndReadByRecipientFalseAndDraftFalseAndTrashedByRecipientFalse(this.currentUser.id()));
    }

    /** A directory of usernames (minus me) for recipient auto-complete. */
    @GetMapping("/directory")
    public List<String> directory() {
        String me = this.currentUser.username();
        return this.users.findAll().stream().map(User::getUsername).filter(u -> !u.equals(me)).sorted().toList();
    }

    /** A full message. Must be a party to it; opening one addressed to me marks it read. */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        Long me = this.currentUser.id();
        Mail m = this.repo.findById(id).orElse(null);
        if (m == null || !(me.equals(m.getFromId()) || me.equals(m.getToId()))) {
            return ResponseEntity.notFound().build();
        }
        if (me.equals(m.getToId()) && !m.isReadByRecipient()) {
            m.setReadByRecipient(true);
            this.repo.save(m);
        }
        return ResponseEntity.ok(summary(m, me));
    }

    /** Send a new message. `to` (username), subject, body. 404 if the recipient is unknown. */
    @PostMapping
    public ResponseEntity<Map<String, Object>> send(@RequestBody MailRequest req) {
        if (req.to() == null || req.to().isBlank()) return ResponseEntity.badRequest().build();
        User recipient = this.users.findByUsername(req.to().trim()).orElse(null);
        if (recipient == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Mail m = deliver(new Mail(), recipient, req);
        return new ResponseEntity<>(summary(m, this.currentUser.id()), HttpStatus.CREATED);
    }

    /** Save a draft (unsent). `to` optional. */
    @PostMapping("/draft")
    public ResponseEntity<Map<String, Object>> saveDraft(@RequestBody MailRequest req) {
        Long me = this.currentUser.id();
        String myName = this.currentUser.username();
        Mail m = new Mail();
        m.setFromId(me);
        m.setFromName(myName);
        m.setToName(req.to());
        m.setSubject(req.subject());
        m.setBody(req.body());
        m.setCreatedAt(System.currentTimeMillis());
        m.setDraft(true);
        return new ResponseEntity<>(summary(this.repo.save(m), me), HttpStatus.CREATED);
    }

    /** Send an existing draft. */
    @PostMapping("/{id}/send")
    public ResponseEntity<Map<String, Object>> sendDraft(@PathVariable Long id, @RequestBody MailRequest req) {
        Long me = this.currentUser.id();
        Mail m = this.repo.findById(id).filter(x -> me.equals(x.getFromId()) && x.isDraft()).orElse(null);
        if (m == null) return ResponseEntity.notFound().build();
        if (req.to() == null || req.to().isBlank()) return ResponseEntity.badRequest().build();
        User recipient = this.users.findByUsername(req.to().trim()).orElse(null);
        if (recipient == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(summary(deliver(m, recipient, req), me));
    }

    /** Mark read / unread (recipient only). */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> setRead(@PathVariable Long id, @RequestParam boolean read) {
        Long me = this.currentUser.id();
        Mail m = this.repo.findById(id).filter(x -> me.equals(x.getToId())).orElse(null);
        if (m == null) return ResponseEntity.notFound().build();
        m.setReadByRecipient(read);
        this.repo.save(m);
        return ResponseEntity.noContent().build();
    }

    /** Trash my copy; deleting an already-trashed message removes it for good. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long me = this.currentUser.id();
        Mail m = this.repo.findById(id).filter(x -> me.equals(x.getFromId()) || me.equals(x.getToId())).orElse(null);
        if (m == null) return ResponseEntity.notFound().build();
        boolean amRecipient = me.equals(m.getToId());
        boolean alreadyTrashed = amRecipient ? m.isTrashedByRecipient() : m.isTrashedBySender();
        if (alreadyTrashed || m.isDraft()) {
            this.repo.delete(m); // already in trash (or a draft) → gone
        } else if (amRecipient) {
            m.setTrashedByRecipient(true);
            this.repo.save(m);
        } else {
            m.setTrashedBySender(true);
            this.repo.save(m);
        }
        return ResponseEntity.noContent().build();
    }

    /** Fill + persist a (new or draft) message as sent, and ping the recipient. */
    private Mail deliver(Mail m, User recipient, MailRequest req) {
        m.setFromId(this.currentUser.id());
        m.setFromName(this.currentUser.username());
        m.setToId(recipient.getId());
        m.setToName(recipient.getUsername());
        m.setSubject(req.subject());
        m.setBody(req.body());
        m.setCreatedAt(System.currentTimeMillis());
        m.setDraft(false);
        m.setReadByRecipient(false);
        Mail saved = this.repo.save(m);
        this.ws.sendToUser(recipient.getUsername(), Map.of(
                "type", "mail",
                "from", m.getFromName(),
                "subject", m.getSubject() == null ? "" : m.getSubject(),
                "mailId", saved.getId()));
        return saved;
    }
}
