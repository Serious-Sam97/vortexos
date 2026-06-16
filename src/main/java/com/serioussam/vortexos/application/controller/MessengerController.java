package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.BuddyDTO;
import com.serioussam.vortexos.application.dto.MessageDTO;
import com.serioussam.vortexos.application.dto.MessengerProfileDTO;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.messenger.Buddy;
import com.serioussam.vortexos.domain.messenger.Message;
import com.serioussam.vortexos.domain.messenger.MessengerProfile;
import com.serioussam.vortexos.domain.profile.Profile;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaBuddyRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaMessageRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaMessengerProfileRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaProfileRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Windows Live Messenger — the buddy list and Messenger identity, scoped to the signed-in
 * user (CurrentUser). The buddy list is persistent (survives sign-outs, shows offline
 * contacts); live online/away presence is layered on the client from the WebSocket feed.
 */
@RestController
@RequestMapping("/messenger")
public class MessengerController {
    private static final int HISTORY_LIMIT = 500; // most-recent messages returned per conversation

    private final JpaBuddyRepository buddies;
    private final JpaMessengerProfileRepository profiles;
    private final JpaUserRepository users;
    private final JpaProfileRepository cloudProfiles;
    private final JpaMessageRepository messages;
    private final CurrentUser currentUser;

    public MessengerController(JpaBuddyRepository buddies, JpaMessengerProfileRepository profiles,
                               JpaUserRepository users, JpaProfileRepository cloudProfiles,
                               JpaMessageRepository messages, CurrentUser currentUser) {
        this.buddies = buddies;
        this.profiles = profiles;
        this.users = users;
        this.cloudProfiles = cloudProfiles;
        this.messages = messages;
        this.currentUser = currentUser;
    }

    private static MessageDTO toDto(Message m) {
        return new MessageDTO(m.getSender(), m.getRecipient(), m.getGroupId(), m.getBody(), m.getCreatedAt());
    }

    /**
     * Conversation history (oldest → newest, capped at the last {@value #HISTORY_LIMIT}).
     * Provide exactly one of: `with=<username>` for a 1:1 chat, or `group=<groupId>` for a group.
     */
    @GetMapping("/history")
    public ResponseEntity<List<MessageDTO>> history(@RequestParam(required = false) String with,
                                                    @RequestParam(required = false) String group) {
        String me = this.currentUser.username();
        List<Message> rows;
        if (with != null && !with.isBlank()) {
            rows = this.messages.conversation(me, with, PageRequest.of(0, HISTORY_LIMIT));
        } else if (group != null && !group.isBlank()) {
            // A groupId is the sorted participant set joined by "|" — only members may read it.
            if (!Arrays.asList(group.split("\\|")).contains(me)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            rows = this.messages.findByGroupIdOrderByCreatedAtDesc(group, PageRequest.of(0, HISTORY_LIMIT));
        } else {
            return ResponseEntity.badRequest().build();
        }
        Collections.reverse(rows); // fetched newest-first; return oldest-first for display
        List<MessageDTO> out = new ArrayList<>(rows.size());
        for (Message m : rows) out.add(toDto(m));
        return ResponseEntity.ok(out);
    }

    /** My buddy list, enriched with each buddy's display name / personal message / picture. */
    @GetMapping("/buddies")
    public List<BuddyDTO> listBuddies() {
        List<Buddy> mine = this.buddies.findByOwnerIdOrderByBuddyUsernameAsc(this.currentUser.id());
        List<BuddyDTO> out = new ArrayList<>(mine.size());
        for (Buddy b : mine) {
            out.add(enrich(b.getBuddyUsername()));
        }
        return out;
    }

    /** Add a buddy by username. 404 if no such user, 400 if it's yourself or blank, 409 if already added. */
    @PostMapping("/buddies")
    public ResponseEntity<BuddyDTO> addBuddy(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        if (username.isBlank()) return ResponseEntity.badRequest().build();
        if (username.equalsIgnoreCase(this.currentUser.username())) return ResponseEntity.badRequest().build();

        User target = this.users.findByUsername(username).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();

        Long owner = this.currentUser.id();
        if (this.buddies.existsByOwnerIdAndBuddyUsername(owner, target.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Buddy b = new Buddy();
        b.setOwnerId(owner);
        b.setBuddyUsername(target.getUsername()); // canonical casing from the user record
        this.buddies.save(b);
        return new ResponseEntity<>(enrich(target.getUsername()), HttpStatus.CREATED);
    }

    /** Remove a buddy. 404 if not on my list. */
    @DeleteMapping("/buddies/{username}")
    public ResponseEntity<Void> removeBuddy(@PathVariable String username) {
        Buddy b = this.buddies.findByOwnerIdAndBuddyUsername(this.currentUser.id(), username).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        this.buddies.delete(b);
        return ResponseEntity.noContent().build();
    }

    /** My Messenger identity (personal message + display picture + status), name from Profile. */
    @GetMapping("/profile")
    public MessengerProfileDTO myProfile() {
        Long uid = this.currentUser.id();
        MessengerProfile p = this.profiles.findByOwnerId(uid).orElse(null);
        MessengerProfileDTO dto = new MessengerProfileDTO();
        dto.setDisplayName(displayNameOf(uid, this.currentUser.username()));
        dto.setPersonalMessage(p != null ? p.getPersonalMessage() : null);
        dto.setDisplayPicture(p != null ? p.getDisplayPicture() : null);
        dto.setStatus(p != null && p.getStatus() != null ? p.getStatus() : "available");
        return dto;
    }

    /** Upsert my Messenger identity (personal message / display picture / status). */
    @PutMapping("/profile")
    public MessengerProfileDTO saveProfile(@RequestBody MessengerProfileDTO dto) {
        Long uid = this.currentUser.id();
        MessengerProfile p = this.profiles.findByOwnerId(uid).orElseGet(MessengerProfile::new);
        p.setOwnerId(uid);
        if (dto.getPersonalMessage() != null) p.setPersonalMessage(dto.getPersonalMessage());
        if (dto.getDisplayPicture() != null) p.setDisplayPicture(dto.getDisplayPicture());
        if (dto.getStatus() != null) p.setStatus(dto.getStatus());
        this.profiles.save(p);
        return myProfile();
    }

    /** Build a rich buddy DTO from a username (display name from Profile, psm/dp from MessengerProfile). */
    private BuddyDTO enrich(String username) {
        User u = this.users.findByUsername(username).orElse(null);
        if (u == null) return new BuddyDTO(username, username, null, null);
        MessengerProfile mp = this.profiles.findByOwnerId(u.getId()).orElse(null);
        return new BuddyDTO(
                u.getUsername(),
                displayNameOf(u.getId(), u.getUsername()),
                mp != null ? mp.getPersonalMessage() : null,
                mp != null ? mp.getDisplayPicture() : null);
    }

    /** A user's display name from their cloud Profile, falling back to the username. */
    private String displayNameOf(Long uid, String username) {
        Profile cp = this.cloudProfiles.findByOwnerId(uid).orElse(null);
        return (cp != null && cp.getDisplayName() != null && !cp.getDisplayName().isBlank())
                ? cp.getDisplayName() : username;
    }
}
