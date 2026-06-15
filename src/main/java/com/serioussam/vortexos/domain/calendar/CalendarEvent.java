package com.serioussam.vortexos.domain.calendar;

import jakarta.persistence.*;

/**
 * A single calendar entry owned by one user. Times are epoch milliseconds:
 * {@code start} is required, {@code endAt} is optional (null for an open-ended
 * or all-day marker). {@code reminderMinutes} of -1 means no reminder.
 */
@Entity
@Table(name = "calendar_event")
public class CalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String title;

    /** Epoch milliseconds. */
    @Column(nullable = false)
    private long start;

    /** Epoch milliseconds; null when the event has no end. */
    @Column
    private Long endAt;

    @Column(nullable = false)
    private boolean allDay;

    @Column(length = 2000)
    private String notes;

    /** Minutes before start to remind; -1 means no reminder. */
    @Column(nullable = false)
    private int reminderMinutes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getStart() { return start; }
    public void setStart(long start) { this.start = start; }

    public Long getEndAt() { return endAt; }
    public void setEndAt(Long endAt) { this.endAt = endAt; }

    public boolean isAllDay() { return allDay; }
    public void setAllDay(boolean allDay) { this.allDay = allDay; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }
}
