package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.calendar.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCalendarRepository extends JpaRepository<CalendarEvent, Long> {
    /** All my events, soonest first. */
    List<CalendarEvent> findByOwnerIdOrderByStartAsc(Long ownerId);

    /** My events starting within [from, to], soonest first. */
    List<CalendarEvent> findByOwnerIdAndStartBetweenOrderByStartAsc(Long ownerId, long from, long to);

    /** A single event of mine. */
    Optional<CalendarEvent> findByIdAndOwnerId(Long id, Long ownerId);
}
