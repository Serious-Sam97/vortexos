package com.serioussam.vortexos.domain.messenger;

import jakarta.persistence.*;

/**
 * Metadata for a Messenger file transfer (Phase 30 follow-up). The bytes live on disk in a
 * dedicated store keyed by {@code id}; only this small row lives in the database. The on-disk
 * file is named by the opaque {@code id} (never the user-supplied name), so a malicious
 * filename can't escape the store. The server stores and serves the bytes but NEVER executes
 * or parses them; downloads go out as attachments.
 */
@Entity
@Table(name = "file_transfer")
public class FileTransfer {
    @Id
    private String id; // UUID — also the on-disk filename

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String name; // original display name (sanitised); used in Content-Disposition

    @Column(nullable = false)
    private long size;

    @Column
    private String contentType;

    @Column(nullable = false)
    private long createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
