package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.mail.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaMailRepository extends JpaRepository<Mail, Long> {
    /** Inbox: delivered (not draft), addressed to me, not trashed by me. */
    List<Mail> findByToIdAndDraftFalseAndTrashedByRecipientFalseOrderByCreatedAtDesc(Long toId);

    /** Sent: delivered, from me, not trashed by me. */
    List<Mail> findByFromIdAndDraftFalseAndTrashedBySenderFalseOrderByCreatedAtDesc(Long fromId);

    /** Drafts: my unsent messages. */
    List<Mail> findByFromIdAndDraftTrueOrderByCreatedAtDesc(Long fromId);

    /** Trash: anything I've trashed (on either side). */
    @Query("select m from Mail m where m.draft = false and " +
            "((m.toId = :uid and m.trashedByRecipient = true) or (m.fromId = :uid and m.trashedBySender = true)) " +
            "order by m.createdAt desc")
    List<Mail> findTrash(@Param("uid") Long uid);

    long countByToIdAndReadByRecipientFalseAndDraftFalseAndTrashedByRecipientFalse(Long toId);
}
