package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.messenger.FileTransfer;
import com.serioussam.vortexos.infrastructure.repository.JpaFileTransferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Messenger file transfer (Phase 30 follow-up). Files are streamed to a disk store (NOT the
 * database) and referenced by an opaque id; the recipient downloads them over HTTP, so big
 * files (up to the configured multipart limit) never touch the chat WebSocket.
 *
 * Safety: the server only STORES and SERVES bytes — it never executes or parses them. The
 * on-disk name is the random id (no path traversal from user input); downloads always go out
 * as attachments; and only the sender or recipient may download a given transfer.
 */
@RestController
@RequestMapping("/messenger/transfer")
public class FileTransferController {

    private final JpaFileTransferRepository transfers;
    private final CurrentUser currentUser;

    @Value("${vortex.transfer.dir:${java.io.tmpdir}/vortex-transfers}")
    private String storageDir;

    private Path root;

    public FileTransferController(JpaFileTransferRepository transfers, CurrentUser currentUser) {
        this.transfers = transfers;
        this.currentUser = currentUser;
    }

    @PostConstruct
    void init() throws IOException {
        this.root = Paths.get(this.storageDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    /** Strip any path/sneaky characters from a user-supplied filename for safe display/download. */
    private static String safeName(String raw) {
        String n = (raw == null || raw.isBlank()) ? "file" : raw;
        n = n.replaceAll("[\\\\/\\r\\n]", "_").trim();
        if (n.isEmpty()) n = "file";
        return n.length() > 150 ? n.substring(0, 150) : n;
    }

    /** Upload a file to send to {@code to}. Streams to disk; returns the transfer reference. */
    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("to") String to) throws IOException {
        if (file.isEmpty() || to == null || to.isBlank()) return ResponseEntity.badRequest().build();

        String id = UUID.randomUUID().toString();
        Path dest = this.root.resolve(id).normalize();
        if (!dest.startsWith(this.root)) return ResponseEntity.badRequest().build(); // defence in depth
        file.transferTo(dest); // streams the (temp) upload to the store without buffering it all in memory

        FileTransfer t = new FileTransfer();
        t.setId(id);
        t.setSender(this.currentUser.username());
        t.setRecipient(to);
        t.setName(safeName(file.getOriginalFilename()));
        t.setSize(file.getSize());
        t.setContentType(file.getContentType());
        t.setCreatedAt(System.currentTimeMillis());
        this.transfers.save(t);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", t.getId(), "name", t.getName(), "size", t.getSize()));
    }

    /** Download a transfer — only the sender or recipient may, and always as an attachment. */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        FileTransfer t = this.transfers.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        String me = this.currentUser.username();
        if (!me.equals(t.getSender()) && !me.equals(t.getRecipient())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Path file = this.root.resolve(id).normalize();
        if (!file.startsWith(this.root) || !Files.exists(file)) return ResponseEntity.notFound().build();

        Resource body = new FileSystemResource(file);
        String disposition = "attachment; filename=\"" + t.getName().replace("\"", "") + "\"";
        return ResponseEntity.ok()
                // Always an attachment + a generic content type → the browser downloads it,
                // never renders/executes it inline.
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(t.getSize())
                .body(body);
    }
}
