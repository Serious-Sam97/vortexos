package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaTaskRepository extends JpaRepository<Task, Long> {
    /** All of a user's tasks, newest first. */
    List<Task> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /** A single task, owner-scoped. */
    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}
