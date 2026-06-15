package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.BbsRequest;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.bbs.BbsPost;
import com.serioussam.vortexos.infrastructure.repository.JpaBbsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * The shared BBS. Posts are authored by a user but the board is PUBLIC — everyone
 * sees every thread. Persistent (unlike presence/chat). Requires auth.
 */
@RestController
@RequestMapping("/bbs")
public class BbsController {
    private final JpaBbsRepository repo;
    private final CurrentUser currentUser;

    public BbsController(JpaBbsRepository repo, CurrentUser currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    /** Top-level threads, newest first, each with its reply count. */
    @GetMapping
    public List<Map<String, Object>> threads() {
        return this.repo.findByParentIdIsNullOrderByCreatedAtDesc().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "title", t.getTitle() == null ? "" : t.getTitle(),
                        "authorName", t.getAuthorName(),
                        "body", t.getBody(),
                        "createdAt", t.getCreatedAt(),
                        "replyCount", this.repo.countByParentId(t.getId())))
                .toList();
    }

    /** A thread with its replies (oldest first). 404 if the id isn't a top-level thread. */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> thread(@PathVariable Long id) {
        BbsPost thread = this.repo.findById(id).filter(p -> p.getParentId() == null).orElse(null);
        if (thread == null) return ResponseEntity.notFound().build();
        List<BbsPost> replies = this.repo.findByParentIdOrderByCreatedAtAsc(id);
        return ResponseEntity.ok(Map.of("thread", thread, "replies", replies));
    }

    /** Create a new thread. Title + body required. */
    @PostMapping
    public ResponseEntity<BbsPost> createThread(@RequestBody BbsRequest req) {
        if (req.title() == null || req.title().isBlank() || req.body() == null || req.body().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(save(null, req.title().trim(), req.body().trim()), HttpStatus.CREATED);
    }

    /** Reply to a thread. Body required; 404 if the thread doesn't exist. */
    @PostMapping("/{id}/reply")
    public ResponseEntity<BbsPost> reply(@PathVariable Long id, @RequestBody BbsRequest req) {
        if (req.body() == null || req.body().isBlank()) return ResponseEntity.badRequest().build();
        boolean threadExists = this.repo.findById(id).filter(p -> p.getParentId() == null).isPresent();
        if (!threadExists) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(save(id, null, req.body().trim()), HttpStatus.CREATED);
    }

    private BbsPost save(Long parentId, String title, String body) {
        BbsPost post = new BbsPost();
        post.setAuthorId(this.currentUser.id());
        post.setAuthorName(this.currentUser.username());
        post.setParentId(parentId);
        post.setTitle(title);
        post.setBody(body);
        post.setCreatedAt(System.currentTimeMillis());
        return this.repo.save(post);
    }
}
