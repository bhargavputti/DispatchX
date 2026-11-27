package com.dispatchx.delivery.service;

import com.dispatchx.common.model.NotificationStatus;
import com.dispatchx.delivery.model.ReconciliationOutcome;
import com.dispatchx.notification.dto.ClaimSnapshot;
import com.dispatchx.notification.dto.NotificationEvidence;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Pure interpretation of the single-row evidence after an ambiguous result commit (plan section 7).
 *
 * <p>Soundness: while a row is PROCESSING at (generation, claim_seq) only the owner of that claim can move it out
 * (the state machine and the claim_seq fence leave no other writer in ), so a non-PROCESSING row carrying the same
 * pair is this claim's own committed result, and the result transaction is atomic. A PROCESSING observation proves only
 * that the result was not visible at read time; no rollback is ever inferred from it.
 */
@Component
public class ReconciliationEvaluator {

    public ReconciliationOutcome evaluate(ClaimSnapshot claim, Optional<NotificationEvidence> evidence) {
        if (evidence.isEmpty()) {
            return ReconciliationOutcome.ABSENT;
        }
        NotificationEvidence row = evidence.get();
        if (row.dispatchGeneration() != claim.generation() || row.claimSeq() != claim.claimSeq()) {
            return ReconciliationOutcome.SUPERSEDED;
        }
        if (row.status() == NotificationStatus.PROCESSING) {
            return ReconciliationOutcome.UNRESOLVED;
        }
        return ReconciliationOutcome.COMMITTED;
    }
}
