package com.serioussam.vortexos.domain.messenger;

import jakarta.persistence.*;

/**
 * A persisted Messenger message — the permanent conversation history (Phase 30 follow-up).
 * One row per message. A 1:1 message has {sender, recipient, groupId = null}; a group message
 * has {sender, groupId, recipient = null}. Identified for de-dup by (groupId, sender, createdAt)
 * because the group multicast fans the same message out as one frame per participant.
 */
@Entity
@Table(name = "message", indexes = {
        @Index(columnList = "recipient, sender"),
        @Index(columnList = "groupId"),
})
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column
    private String recipient; // null for group messages

    @Column
    private String groupId; // null for 1:1 messages

    @Column(length = 4000, nullable = false)
    private String body;

    @Column(nullable = false)
    private long createdAt; // epoch ms (the client send time, kept consistent across relay + history)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
