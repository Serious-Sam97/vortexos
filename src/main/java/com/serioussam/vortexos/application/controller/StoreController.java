package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.AppPublishRequest;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.store.AppListing;
import com.serioussam.vortexos.infrastructure.repository.JpaAppListingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * The cloud App Store. The catalogue is PUBLIC to any signed-in user (browse +
 * install); publishing is owner-scoped — re-publishing an appId you don't own is
 * forbidden. The full .vxapp package travels as a JSON string.
 */
@RestController
@RequestMapping("/store")
public class StoreController {
    private final JpaAppListingRepository repo;
    private final CurrentUser currentUser;

    public StoreController(JpaAppListingRepository repo, CurrentUser currentUser) {
        this.repo = repo;
        this.currentUser = currentUser;
    }

    private static Map<String, Object> summary(AppListing a) {
        return Map.of(
                "appId", a.getAppId(),
                "name", a.getName(),
                "version", a.getVersion(),
                "author", a.getAuthor(),
                "description", a.getDescription() == null ? "" : a.getDescription(),
                "icon", a.getIcon() == null ? "" : a.getIcon(),
                "installs", a.getInstalls(),
                "updatedAt", a.getUpdatedAt());
    }

    /** The whole catalogue, most-installed first. */
    @GetMapping
    public List<Map<String, Object>> list() {
        return this.repo.findByOrderByInstallsDescUpdatedAtDesc().stream().map(StoreController::summary).toList();
    }

    /** The current user's published apps. */
    @GetMapping("/mine")
    public List<Map<String, Object>> mine() {
        return this.repo.findByAuthorIdOrderByUpdatedAtDesc(this.currentUser.id()).stream().map(StoreController::summary).toList();
    }

    /** A full listing (with the package) for install. 404 if not found. */
    @GetMapping("/{appId}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String appId) {
        return this.repo.findByAppId(appId)
                .map(a -> {
                    Map<String, Object> m = new java.util.HashMap<>(summary(a));
                    m.put("packageJson", a.getPackageJson());
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Publish or update an app. Owner-scoped: can't overwrite another user's appId. */
    @PostMapping
    public ResponseEntity<Map<String, Object>> publish(@RequestBody AppPublishRequest req) {
        if (req.appId() == null || req.appId().isBlank()
                || req.name() == null || req.name().isBlank()
                || req.version() == null || req.version().isBlank()
                || req.packageJson() == null || req.packageJson().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Long uid = this.currentUser.id();
        String uname = this.currentUser.username();
        long now = System.currentTimeMillis();

        AppListing existing = this.repo.findByAppId(req.appId()).orElse(null);
        if (existing != null && !existing.getAuthorId().equals(uid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // not your app
        }
        AppListing a = existing != null ? existing : new AppListing();
        if (existing == null) {
            a.setAppId(req.appId());
            a.setAuthorId(uid);
            a.setAuthor(uname);
            a.setInstalls(0);
            a.setCreatedAt(now);
        }
        a.setName(req.name());
        a.setVersion(req.version());
        a.setDescription(req.description());
        a.setIcon(req.icon());
        a.setPackageJson(req.packageJson());
        a.setUpdatedAt(now);
        AppListing saved = this.repo.save(a);
        return new ResponseEntity<>(summary(saved), existing == null ? HttpStatus.CREATED : HttpStatus.OK);
    }

    /** Record an install (analytics). Returns the new count. 404 if unknown. */
    @PostMapping("/{appId}/install")
    public ResponseEntity<Map<String, Object>> install(@PathVariable String appId) {
        AppListing a = this.repo.findByAppId(appId).orElse(null);
        if (a == null) return ResponseEntity.notFound().build();
        a.setInstalls(a.getInstalls() + 1);
        this.repo.save(a);
        return ResponseEntity.ok(Map.of("installs", a.getInstalls()));
    }

    /** Unpublish — author only. 404 if not found or not yours. */
    @DeleteMapping("/{appId}")
    public ResponseEntity<Void> unpublish(@PathVariable String appId) {
        AppListing a = this.repo.findByAppId(appId).filter(x -> x.getAuthorId().equals(this.currentUser.id())).orElse(null);
        if (a == null) return ResponseEntity.notFound().build();
        this.repo.delete(a);
        return ResponseEntity.noContent().build();
    }
}
