package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.domain.file.FileRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JpaFileRepository extends JpaRepository<File, Long>, FileRepository {
    List<File> findByOwnerId(Long ownerId);

    Optional<File> findByPathAndOwnerId(String path, Long ownerId);

    List<File> findByPathStartingWithAndOwnerId(String prefix, Long ownerId);

    // ── Network Neighborhood: per-user shares ────────────────────────────
    List<File> findByOwnerIdAndSharedTrue(Long ownerId);

    /** Owner ids that have at least one shared file — i.e. who appears in the Neighborhood. */
    @Query("select distinct f.ownerId from File f where f.shared = true")
    List<Long> findDistinctSharedOwnerIds();
}
