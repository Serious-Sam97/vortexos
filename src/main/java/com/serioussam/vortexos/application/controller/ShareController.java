package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.UserSummary;
import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaFileRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaUserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The Network Neighborhood: read access to other users' *shared* files. Any signed-in user
 * can list who is sharing and read those users' shared files — but never their private ones
 * (a file is only visible here if its owner set shared = true; see FileController#setShared).
 */
@RestController
@RequestMapping("/share")
public class ShareController {

    private final JpaFileRepository fileRepository;
    private final JpaUserRepository userRepository;

    public ShareController(JpaFileRepository fileRepository, JpaUserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    /** Users who have at least one shared file. */
    @GetMapping("/users")
    public List<UserSummary> sharers() {
        List<Long> ownerIds = this.fileRepository.findDistinctSharedOwnerIds();
        return this.userRepository.findAllById(ownerIds).stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername()))
                .toList();
    }

    /** A given user's shared files (with content, so they can be opened read-only). */
    @GetMapping("/users/{ownerId}/files")
    public List<File> shares(@PathVariable("ownerId") Long ownerId) {
        return this.fileRepository.findByOwnerIdAndSharedTrue(ownerId);
    }
}
