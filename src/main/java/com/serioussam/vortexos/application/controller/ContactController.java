package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.ContactRequest;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.contact.Contact;
import com.serioussam.vortexos.infrastructure.repository.JpaContactRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Address book — private contacts owned by the signed-in user (CurrentUser).
 * Every operation is scoped to ownerId, so users only ever see their own entries.
 */
@RestController
@RequestMapping("/contacts")
public class ContactController {
    private final JpaContactRepository repo;
    private final CurrentUser currentUser;

    public ContactController(JpaContactRepository repo, CurrentUser currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    /** My contacts, alphabetised. */
    @GetMapping
    public List<Contact> list() {
        return this.repo.findByOwnerIdOrderByNameAsc(this.currentUser.id());
    }

    /** Create a contact. 400 if name is blank. */
    @PostMapping
    public ResponseEntity<Contact> create(@RequestBody ContactRequest req) {
        if (req.name() == null || req.name().isBlank()) return ResponseEntity.badRequest().build();
        Contact c = new Contact();
        c.setOwnerId(this.currentUser.id());
        c.setName(req.name());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setNotes(req.notes());
        return new ResponseEntity<>(this.repo.save(c), HttpStatus.CREATED);
    }

    /** Update one of my contacts. 404 if not mine, 400 if name is blank. */
    @PutMapping("/{id}")
    public ResponseEntity<Contact> update(@PathVariable Long id, @RequestBody ContactRequest req) {
        Contact c = this.repo.findByIdAndOwnerId(id, this.currentUser.id()).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        if (req.name() == null || req.name().isBlank()) return ResponseEntity.badRequest().build();
        c.setName(req.name());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setNotes(req.notes());
        return ResponseEntity.ok(this.repo.save(c));
    }

    /** Delete one of my contacts. 404 if not mine. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Contact c = this.repo.findByIdAndOwnerId(id, this.currentUser.id()).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        this.repo.delete(c);
        return ResponseEntity.noContent().build();
    }
}
