package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.messenger.OfflineMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOfflineMessageRepository extends JpaRepository<OfflineMessage, Long> {
    /** A user's queued offline messages, oldest first. */
    List<OfflineMessage> findByRecipientOrderByCreatedAtAsc(String recipient);
}
