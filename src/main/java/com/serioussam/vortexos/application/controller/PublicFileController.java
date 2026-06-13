package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.FileDTO;
import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.infrastructure.repository.JpaFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * The shared public drive behind the OS's /mnt/public mount. Unlike {@link FileController}
 * these endpoints are NOT scoped to the caller — every signed-in user reads and writes the
 * same files. Public files are stored as ordinary {@link File} rows owned by the reserved
 * {@link #PUBLIC_OWNER} id, so they never collide with anyone's private /mnt/cloud files
 * (different owner, different path prefix). Authentication is still required (Spring
 * Security gates everything outside /auth/**).
 */
@RestController
@RequestMapping("/public/files")
public class PublicFileController {

    /** Reserved owner id for the shared public drive (no real user has id 0). */
    public static final Long PUBLIC_OWNER = 0L;

    private final JpaFileRepository fileRepository;

    public PublicFileController(JpaFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @GetMapping
    public List<File> index() {
        return this.fileRepository.findByOwnerId(PUBLIC_OWNER);
    }

    @PostMapping
    public ResponseEntity<File> upsert(@RequestBody FileDTO dto) {
        if (dto.getPath() == null || dto.getPath().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        File file = this.fileRepository.findByPathAndOwnerId(dto.getPath(), PUBLIC_OWNER).orElseGet(File::new);
        file.setOwnerId(PUBLIC_OWNER);
        file.setPath(dto.getPath());
        file.setName(dto.getName());
        file.setType(dto.getType() != null ? dto.getType() : "file");
        file.setContent(dto.getContent());
        file.setMetadata(dto.getMetadata());
        if (file.getCreatedDate() == null) {
            file.setCreatedDate(LocalDate.now());
        }
        file.setUpdatedDate(LocalDate.now());

        File saved = this.fileRepository.save(file);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("path") String path) {
        if (path == null || path.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        this.fileRepository.findByPathAndOwnerId(path, PUBLIC_OWNER).ifPresent(this.fileRepository::delete);
        this.fileRepository.deleteAll(this.fileRepository.findByPathStartingWithAndOwnerId(path + "/", PUBLIC_OWNER));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/rename")
    public ResponseEntity<Void> rename(@RequestParam("from") String from, @RequestParam("to") String to) {
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        this.fileRepository.findByPathAndOwnerId(from, PUBLIC_OWNER).ifPresent(file -> {
            file.setPath(to);
            file.setName(to.substring(to.lastIndexOf('/') + 1));
            file.setUpdatedDate(LocalDate.now());
            this.fileRepository.save(file);
        });

        for (File child : this.fileRepository.findByPathStartingWithAndOwnerId(from + "/", PUBLIC_OWNER)) {
            child.setPath(to + child.getPath().substring(from.length()));
            child.setUpdatedDate(LocalDate.now());
            this.fileRepository.save(child);
        }
        return ResponseEntity.noContent().build();
    }
}
