package com.dispatchx.dlq.repository;

import com.dispatchx.dlq.entity.DlqQuarantineEntity;
import com.dispatchx.dlq.model.QuarantineAssociation;
import com.dispatchx.dlq.model.QuarantineStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Keyset search over quarantined records (/ , ).
 *
 * <p>Criteria-based for the same reason as {@link DeadLetterEntrySearch}: a parameter that appears only inside
 * {@code ? is null} has no inferable type in PostgreSQL.
 */
public interface DlqQuarantineSearch {

    List<DlqQuarantineEntity> searchQuarantine(
            QuarantineAssociation association,
            QuarantineStatus status,
            String topic,
            String dltReason,
            Instant from,
            Instant to,
            Instant cursorAt,
            UUID cursorId,
            int limit
    );
}
