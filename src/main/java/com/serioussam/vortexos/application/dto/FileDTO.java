package com.serioussam.vortexos.application.dto;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public class FileDTO {
    @Nullable
    private Long id;

    private String name;
    private String path;
    private String type;

    @Nullable
    private byte[] content;

    @Nullable
    private String metadata;

    @Nullable
    private LocalDate createdDate;

    @Nullable
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
