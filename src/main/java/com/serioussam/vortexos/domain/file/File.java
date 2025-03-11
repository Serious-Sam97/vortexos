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

    @Column(nullable = false)
    private String type; // "file" or "folder"

    @Lob
    @Column(name = "content")
    private byte[] content;  // Stores file content (NULL for folders)

    @Lob
    @Column(name = "metadata", columnDefinition = "TEXT")  // JSON as TEXT
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

    public void setContent(byte[] content)
    {
        this.content = content;
    }

    public byte[] getContent()
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
