package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.bbs.BbsPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaBbsRepository extends JpaRepository<BbsPost, Long> {
    /** Top-level threads, newest first. */
    List<BbsPost> findByParentIdIsNullOrderByCreatedAtDesc();

    /** Replies to a thread, oldest first. */
    List<BbsPost> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByParentId(Long parentId);
}
