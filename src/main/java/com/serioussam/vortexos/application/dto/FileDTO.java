package com.serioussam.vortexos.application.dto;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

public class FileDTO {
    @Nullable
    private Long id;

    private String name;
    private String path;

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
