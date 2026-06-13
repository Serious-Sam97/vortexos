package com.serioussam.vortexos.domain.file;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "file")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    // Owning user's id. A plain column (not a @ManyToOne) so the User — and its password
    // hash — is never serialized into a /files response.
    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String type; // "file" or "folder"

    // Base64-encoded content as TEXT. SQLite's JDBC driver does not support LOB reads,
    // so we avoid @Lob/byte[] and store the content as a plain (large) text column.
    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // NULL for folders

    @Column(name = "metadata", columnDefinition = "TEXT") // JSON as TEXT
    private String metadata;

    @Column(nullable = false)
    private LocalDate createdDate;

    @Column(nullable = false)
    private LocalDate updatedDate;

    public Long getId()
    {
        return this.id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return this.content;
    }

    public void setMetadata(String metadata)
    {
        this.metadata = metadata;
    }

    public String getMetadata()
    {
        return this.metadata;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return this.path;
    }

    public void setOwnerId(Long ownerId)
    {
        this.ownerId = ownerId;
    }

    public Long getOwnerId()
    {
        return this.ownerId;
    }

    public void setCreatedDate(LocalDate createdDate)
    {
        this.createdDate = createdDate;
    }

    public LocalDate getCreatedDate()
    {
        return this.createdDate;
    }

    public void setUpdatedDate(LocalDate updatedDate)
    {
        this.updatedDate = updatedDate;
    }

    public LocalDate getUpdatedDate()
    {
        return this.updatedDate;
    }
}
