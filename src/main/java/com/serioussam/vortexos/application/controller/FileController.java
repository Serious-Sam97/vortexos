package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.FileDTO;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.infrastructure.repository.JpaFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final JpaFileRepository fileRepository;
    private final CurrentUser currentUser;

    public FileController(JpaFileRepository fileRepository, CurrentUser currentUser) {
        this.fileRepository = fileRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<File> index() {
        return this.fileRepository.findByOwnerId(this.currentUser.id());
    }

    /** Create or update a file/folder, keyed by its path, scoped to the current user. */
    @PostMapping
    public ResponseEntity<File> upsert(@RequestBody FileDTO dto) {
        if (dto.getPath() == null || dto.getPath().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Long ownerId = this.currentUser.id();
        File file = this.fileRepository.findByPathAndOwnerId(dto.getPath(), ownerId).orElseGet(File::new);
        file.setOwnerId(ownerId);
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

    /** Delete a path and everything beneath it (folders delete recursively). */
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("path") String path) {
        if (path == null || path.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Long ownerId = this.currentUser.id();
        this.fileRepository.findByPathAndOwnerId(path, ownerId).ifPresent(this.fileRepository::delete);
        this.fileRepository.deleteAll(this.fileRepository.findByPathStartingWithAndOwnerId(path + "/", ownerId));
        return ResponseEntity.noContent().build();
    }

    /** Move/rename a path (and any descendants) by rewriting their path prefix. */
    @PutMapping("/rename")
    public ResponseEntity<Void> rename(@RequestParam("from") String from, @RequestParam("to") String to) {
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Long ownerId = this.currentUser.id();
        this.fileRepository.findByPathAndOwnerId(from, ownerId).ifPresent(file -> {
            file.setPath(to);
            file.setName(to.substring(to.lastIndexOf('/') + 1));
            file.setUpdatedDate(LocalDate.now());
            this.fileRepository.save(file);
        });

        for (File child : this.fileRepository.findByPathStartingWithAndOwnerId(from + "/", ownerId)) {
            child.setPath(to + child.getPath().substring(from.length()));
            child.setUpdatedDate(LocalDate.now());
            this.fileRepository.save(child);
        }
        return ResponseEntity.noContent().build();
    }
}
