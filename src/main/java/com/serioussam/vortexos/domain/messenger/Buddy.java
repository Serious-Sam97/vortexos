package com.serioussam.vortexos.domain.messenger;

import jakarta.persistence.*;

/**
 * A single entry in a user's Messenger contact (buddy) list: ownerId added the user
 * `buddyUsername` to their list. Persistent, so the buddy list survives sign-outs and
 * shows offline contacts too (live presence is layered on top via the WebSocket). One row
 * per (owner, buddy) pair.
 */
@Entity
@Table(name = "buddy", uniqueConstraints = @UniqueConstraint(columnNames = {"ownerId", "buddyUsername"}))
public class Buddy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String buddyUsername;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getBuddyUsername() { return buddyUsername; }
    public void setBuddyUsername(String buddyUsername) { this.buddyUsername = buddyUsername; }
}
