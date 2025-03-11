package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.file.File;
import com.serioussam.vortexos.domain.file.FileRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFileRepository  extends JpaRepository<File, Long>, FileRepository {
}
