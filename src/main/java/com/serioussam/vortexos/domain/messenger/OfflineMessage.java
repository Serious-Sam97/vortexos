package com.serioussam.vortexos.domain.messenger;

import jakarta.persistence.*;

/**
 * A Messenger message sent to a user who was offline at the time (Phase 30 · M3). Held until
 * the recipient next connects, then delivered as a normal `msg` frame and removed. Keyed by
 * recipient username so delivery is a simple lookup on connection.
 */
@Entity
@Table(name = "offline_message", indexes = @Index(columnList = "recipient"))
public class OfflineMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String sender;

    @Column(length = 4000, nullable = false)
    private String body;

    @Column(nullable = false)
    private long createdAt; // epoch ms

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
