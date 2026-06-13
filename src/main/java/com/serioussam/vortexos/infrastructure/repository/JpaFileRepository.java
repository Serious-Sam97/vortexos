package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.domain.file.FileRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaFileRepository extends JpaRepository<File, Long>, FileRepository {
    List<File> findByOwnerId(Long ownerId);

    Optional<File> findByPathAndOwnerId(String path, Long ownerId);

    List<File> findByPathStartingWithAndOwnerId(String prefix, Long ownerId);
}
