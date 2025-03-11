package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.platform.Platform;
import com.serioussam.vortexos.domain.platform.PlatformRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPlatformRepository extends JpaRepository<Platform, Long>, PlatformRepository {
}
