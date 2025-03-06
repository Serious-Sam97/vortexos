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
