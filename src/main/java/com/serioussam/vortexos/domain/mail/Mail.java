package com.serioussam.vortexos.domain.mail;

import jakarta.persistence.*;

/**
 * One mail message between two users. A single row is shared by both parties:
 * the recipient sees it in their Inbox (toId), the sender in Sent (fromId). Soft
 * delete is per-side (trashedBySender / trashedByRecipient). A draft is the
 * sender's unsent message (draft = true, toId may be null until sent).
 */
@Entity
@Table(name = "mail")
public class Mail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromId;

    @Column(nullable = false)
    private String fromName;

    /** Null only while a draft. */
    @Column
    private Long toId;

    @Column
    private String toName;

    @Column
    private String subject;

    @Column(length = 8000)
    private String body;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private boolean readByRecipient;

    @Column(nullable = false)
    private boolean trashedBySender;

    @Column(nullable = false)
    private boolean trashedByRecipient;

    @Column(nullable = false)
    private boolean draft;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFromId() { return fromId; }
    public void setFromId(Long fromId) { this.fromId = fromId; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public Long getToId() { return toId; }
    public void setToId(Long toId) { this.toId = toId; }

    public String getToName() { return toName; }
    public void setToName(String toName) { this.toName = toName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isReadByRecipient() { return readByRecipient; }
    public void setReadByRecipient(boolean readByRecipient) { this.readByRecipient = readByRecipient; }

    public boolean isTrashedBySender() { return trashedBySender; }
    public void setTrashedBySender(boolean trashedBySender) { this.trashedBySender = trashedBySender; }

    public boolean isTrashedByRecipient() { return trashedByRecipient; }
    public void setTrashedByRecipient(boolean trashedByRecipient) { this.trashedByRecipient = trashedByRecipient; }

    public boolean isDraft() { return draft; }
    public void setDraft(boolean draft) { this.draft = draft; }
}
