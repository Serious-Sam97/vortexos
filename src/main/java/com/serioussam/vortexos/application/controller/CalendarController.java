package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.CalendarEventRequest;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.calendar.CalendarEvent;
import com.serioussam.vortexos.infrastructure.repository.JpaCalendarRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VortexCalendar — personal calendar events. Every operation is scoped to the
 * signed-in user (CurrentUser); a user only ever sees and mutates their own events.
 */
@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final JpaCalendarRepository repo;
    private final CurrentUser currentUser;

    public CalendarController(JpaCalendarRepository repo, CurrentUser currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    /** List my events; if both from & to are given, only those starting in that window. */
    @GetMapping
    public List<CalendarEvent> list(@RequestParam(required = false) Long from, @RequestParam(required = false) Long to) {
        Long me = this.currentUser.id();
        if (from != null && to != null) {
            return this.repo.findByOwnerIdAndStartBetweenOrderByStartAsc(me, from, to);
        }
        return this.repo.findByOwnerIdOrderByStartAsc(me);
    }

    /** Create a new event. 400 if the title is blank. */
    @PostMapping
    public ResponseEntity<CalendarEvent> create(@RequestBody CalendarEventRequest req) {
        if (req.title() == null || req.title().isBlank()) return ResponseEntity.badRequest().build();
        CalendarEvent e = new CalendarEvent();
        e.setOwnerId(this.currentUser.id());
        e.setTitle(req.title());
        e.setStart(req.start());
        e.setEndAt(req.endAt());
        e.setAllDay(req.allDay());
        e.setNotes(req.notes());
        e.setReminderMinutes(req.reminderMinutes());
        return new ResponseEntity<>(this.repo.save(e), HttpStatus.CREATED);
    }

    /** Update one of my events. 404 if it isn't mine; 400 if the new title is blank. */
    @PutMapping("/{id}")
    public ResponseEntity<CalendarEvent> update(@PathVariable Long id, @RequestBody CalendarEventRequest req) {
        Long me = this.currentUser.id();
        CalendarEvent e = this.repo.findByIdAndOwnerId(id, me).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();
        if (req.title() == null || req.title().isBlank()) return ResponseEntity.badRequest().build();
        e.setTitle(req.title());
        e.setStart(req.start());
        e.setEndAt(req.endAt());
        e.setAllDay(req.allDay());
        e.setNotes(req.notes());
        e.setReminderMinutes(req.reminderMinutes());
        return ResponseEntity.ok(this.repo.save(e));
    }

    /** Delete one of my events. 404 if it isn't mine. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long me = this.currentUser.id();
        CalendarEvent e = this.repo.findByIdAndOwnerId(id, me).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();
        this.repo.delete(e);
        return ResponseEntity.noContent().build();
    }
}
