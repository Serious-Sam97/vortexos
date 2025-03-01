package com.serioussam.vortexos.domain.game;

import com.serioussam.vortexos.domain.platform.Platform;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "platform_id", nullable = false) // Foreign key column
    private Platform platform;

    @Column(nullable = true)
    private LocalDate startedDate;

    @Column(nullable = true)
    private LocalDate completedDate;

    @Column(nullable = true)
    private boolean completed;

    @Column(nullable = false)
    private boolean backlog = false;

    @Column(nullable = true)
    private String notes;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(LocalDate startedDate) {
        this.startedDate = startedDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setPlatform(Platform platform)
    {
        this.platform = platform;
    }

    public Platform getPlatform()
    {
        return this.platform;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public String getNotes()
    {
        return this.notes;
    }

    public void setBacklog(boolean backlog)
    {
        this.backlog = backlog;
    }

    public boolean getBacklog()
    {
        return this.backlog;
    }
}
