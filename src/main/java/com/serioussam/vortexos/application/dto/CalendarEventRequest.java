package com.serioussam.vortexos.application.dto;

/** Create / update a calendar event. Times are epoch milliseconds; reminderMinutes -1 means none. */
public record CalendarEventRequest(String title, long start, Long endAt, boolean allDay, String notes, int reminderMinutes) {}
