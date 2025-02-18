package com.serioussam.gamecache.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;

import java.time.LocalDate;

public class GameDTO {

    @Nullable
    private Long id;
    private String title;

    @JsonProperty("platform_id")
    private Long platform_id;

    @Nullable
    private LocalDate startedDate;

    @Nullable
    private LocalDate completedDate;

    @Nullable
    private boolean completed;

    @Nullable
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

    public Long getPlatformId() {
        return this.platform_id;
    }

    public void setPlatformId(Long platform_id) {
        this.platform_id = platform_id;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public String getNotes()
    {
        return this.notes;
    }
}
