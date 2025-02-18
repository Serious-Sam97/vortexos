package com.serioussam.gamecache.infrastructure.repository;

import com.serioussam.gamecache.domain.platform.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPlatformRepository extends JpaRepository<Platform, Long> {
}
