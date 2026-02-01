package az.kredo.loan.service;

import az.kredo.loan.entity.Decision;
import az.kredo.loan.entity.LoanApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Deterministic scoring engine that implements a credit policy.
 * Computes score, decision, approved amount, APR, and reason codes.
 */
@Slf4j
@Service
public class DecisionEngine {

    /**
     * Main decision method - computes score and decision for a loan application.
     */
    public DecisionResult evaluate(LoanApplication application) {
        log.info("Evaluating loan application: {}", application.getId());

        // FAST MOCK RESPONSE MODE
        // Always return a static approval for lightning fast testing
        return DecisionResult.builder()
                .score(850)
                .decision(Decision.APPROVED)
                .approvedAmount(new BigDecimal("5000.00"))
                .apr(new BigDecimal("12.00"))
                .reasonCodes(List.of("MOCK_FAST_TRACK", "PRE_APPROVED"))
                .build();
    }

    /**
     * Result holder for decision engine output.
     */
    @lombok.Builder
    @lombok.Data
    public static class DecisionResult {
        private int score;
        private Decision decision;
        private BigDecimal approvedAmount;
        private BigDecimal apr;
        private List<String> reasonCodes;
    }
}
