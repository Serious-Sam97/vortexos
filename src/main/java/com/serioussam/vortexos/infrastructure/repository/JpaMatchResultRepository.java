package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.match.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
