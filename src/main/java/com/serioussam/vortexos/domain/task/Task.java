package com.serioussam.vortexos.domain.task;

import jakarta.persistence.*;

/**
 * A single to-do item owned by one user. Every row is scoped to its owner
 * (ownerId); priority is 0=low, 1=normal, 2=high and dueAt is an optional epoch
 * millisecond timestamp.
 */
@Entity
@Table(name = "todo_task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean done;

    /** Optional due date, epoch milliseconds. */
    @Column
    private Long dueAt;

    /** 0 = low, 1 = normal, 2 = high. */
    @Column(nullable = false)
    private int priority;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private long createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public Long getDueAt() { return dueAt; }
    public void setDueAt(Long dueAt) { this.dueAt = dueAt; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
