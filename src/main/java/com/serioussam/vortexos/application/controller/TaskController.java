package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.TaskRequest;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.task.Task;
import com.serioussam.vortexos.infrastructure.repository.JpaTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VortexTasks — a per-user to-do list. Every operation is scoped to the signed-in
 * user (CurrentUser); a user can only see and mutate their own tasks.
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final JpaTaskRepository repo;
    private final CurrentUser currentUser;

    public TaskController(JpaTaskRepository repo, CurrentUser currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    /** List all of my tasks, newest first. */
    @GetMapping
    public List<Task> list() {
        return this.repo.findByOwnerIdOrderByCreatedAtDesc(this.currentUser.id());
    }

    /** Create a new task. 400 if the title is blank. */
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody TaskRequest req) {
        if (req.title() == null || req.title().isBlank()) return ResponseEntity.badRequest().build();
        Long uid = this.currentUser.id();
        Task t = new Task();
        t.setOwnerId(uid);
        t.setTitle(req.title());
        t.setDone(req.done());
        t.setDueAt(req.dueAt());
        t.setPriority(req.priority());
        t.setNotes(req.notes());
        t.setCreatedAt(System.currentTimeMillis());
        return new ResponseEntity<>(this.repo.save(t), HttpStatus.CREATED);
    }

    /** Update an existing task (mine only). 404 if not found, 400 if the title is blank. */
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody TaskRequest req) {
        Task t = this.repo.findByIdAndOwnerId(id, this.currentUser.id()).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        if (req.title() == null || req.title().isBlank()) return ResponseEntity.badRequest().build();
        t.setTitle(req.title());
        t.setDone(req.done());
        t.setDueAt(req.dueAt());
        t.setPriority(req.priority());
        t.setNotes(req.notes());
        return ResponseEntity.ok(this.repo.save(t));
    }

    /** Delete a task (mine only). 404 if not found. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Task t = this.repo.findByIdAndOwnerId(id, this.currentUser.id()).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        this.repo.delete(t);
        return ResponseEntity.noContent().build();
    }
}
