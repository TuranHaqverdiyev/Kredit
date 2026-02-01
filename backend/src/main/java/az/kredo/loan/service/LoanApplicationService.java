package az.kredo.loan.service;

import az.kredo.loan.dto.loan.*;
import az.kredo.loan.entity.ApplicationStatus;
import az.kredo.loan.entity.EmploymentStatus;
import az.kredo.loan.entity.LoanApplication;
import az.kredo.loan.exception.LoanApplicationException;
import az.kredo.loan.integration.CRMClient;
import az.kredo.loan.repository.LoanApplicationRepository;
import az.kredo.loan.security.FieldEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final FieldEncryptor fieldEncryptor;
    private final DecisionEngine decisionEngine;
    private final CRMClient crmClient;

    /**
     * Create a new loan application with personal and financial information.
     */
    @Transactional
    public ApplyToLoanResponse applyToLoan(ApplyToLoanRequest request, String authenticatedPhone) {
        log.info("Processing loan application for phone [MASKED]");

        // Verify the request phone matches the authenticated phone
        if (!request.getPhoneNumber().equals(authenticatedPhone)) {
            log.warn("Phone number mismatch: request vs authenticated");
            throw LoanApplicationException.unauthorized();
        }

        // Check for existing active application (idempotency)
        if (loanApplicationRepository.hasActiveApplication(request.getPhoneNumber())) {
            log.info("Duplicate application attempt for phone [MASKED]");
            throw LoanApplicationException.duplicateApplication();
        }

        // Create the loan application
        LoanApplication application = LoanApplication.builder()
                .phoneNumber(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .employmentStatus(EmploymentStatus.valueOf(request.getEmploymentStatus()))
                .monthlyIncome(request.getMonthlyIncome())
                .existingMonthlyDebt(request.getExistingMonthlyDebt())
                .termsAccepted(request.getConsent().isTermsAccepted())
                .privacyAccepted(request.getConsent().isPrivacyAccepted())
                .consentTimestamp(Instant.now())
                .status(ApplicationStatus.INFO_SUBMITTED)
                .build();

        // Encrypt sensitive fields
        application.setFin(request.getFin(), fieldEncryptor);
        application.setAddress(request.getAddress(), fieldEncryptor);

        LoanApplication saved = loanApplicationRepository.save(application);
        log.info("Loan application created: {}", saved.getId());

        // Async: Push to CRM
        pushToCrmAsync(saved);

        return ApplyToLoanResponse.builder()
                .applicationId(saved.getId())
                .status(saved.getStatus().name())
                .build();
    }

    /**
     * Submit the requested loan amount and trigger scoring.
     */
    @Transactional
    public SubmitAmountResponse submitRequestedAmount(UUID applicationId,
            SubmitAmountRequest request,
            String authenticatedPhone) {
        log.info("Submitting requested amount for application: {}", applicationId);

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> LoanApplicationException.notFound(applicationId.toString()));

        // Verify ownership
        if (!application.getPhoneNumber().equals(authenticatedPhone)) {
            log.warn("Unauthorized access attempt to application: {}", applicationId);
            throw LoanApplicationException.unauthorized();
        }

        // Verify correct status
        if (application.getStatus() != ApplicationStatus.INFO_SUBMITTED) {
            throw LoanApplicationException.invalidStatus(
                    application.getStatus().name(),
                    ApplicationStatus.INFO_SUBMITTED.name());
        }

        // Update with requested amount
        application.setRequestedAmount(request.getRequestedAmount());
        application.setTermMonths(request.getTermMonths());
        application.setStatus(ApplicationStatus.SCORING);

        loanApplicationRepository.save(application);
        log.info("Application {} moved to SCORING status", applicationId);

        // Synchronous scoring for immediate response
        processDecision(application);

        return SubmitAmountResponse.builder()
                .applicationId(applicationId)
                .status(application.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public LoanResultResponse getResult(UUID applicationId, String authenticatedPhone) {
        log.info("Fetching result for application: {}", applicationId);

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> LoanApplicationException.notFound(applicationId.toString()));

        // Verify ownership
        if (!application.getPhoneNumber().equals(authenticatedPhone)) {
            log.warn("Unauthorized access attempt to application: {}", applicationId);
            throw LoanApplicationException.unauthorized();
        }

        return LoanResultResponse.builder()
                .applicationId(application.getId())
                .status(application.getStatus().name())
                .decision(application.getDecision() != null ? application.getDecision().name() : null)
                .score(application.getScore())
                .approvedAmount(application.getApprovedAmount())
                .apr(application.getApr())
                .reasonCodes(application.getReasonCodes())
                .lastUpdated(application.getUpdatedAt())
                .build();
    }

    /**
     * Accept the presented loan offer.
     */
    @Transactional
    public void acceptOffer(UUID applicationId, String authenticatedPhone) {
        log.info("Accepting offer for application: {}", applicationId);
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> LoanApplicationException.notFound(applicationId.toString()));

        if (!application.getPhoneNumber().equals(authenticatedPhone)) {
            throw LoanApplicationException.unauthorized();
        }

        application.setStatus(ApplicationStatus.OFFER_ACCEPTED);
        loanApplicationRepository.save(application);
    }

    /**
     * Reject the presented loan offer.
     */
    @Transactional
    public void rejectOffer(UUID applicationId, String authenticatedPhone) {
        log.info("Rejecting offer for application: {}", applicationId);
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> LoanApplicationException.notFound(applicationId.toString()));

        if (!application.getPhoneNumber().equals(authenticatedPhone)) {
            throw LoanApplicationException.unauthorized();
        }

        application.setStatus(ApplicationStatus.OFFER_REJECTED);
        application.setDecision(az.kredo.loan.entity.Decision.CUSTOMER_REJECTED);
        loanApplicationRepository.save(application);
    }

    /**
     * Finalize the loan application (after signature/KYC).
     */
    @Transactional
    public void finalizeApplication(UUID applicationId, String authenticatedPhone) {
        log.info("Finalizing application: {}", applicationId);
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> LoanApplicationException.notFound(applicationId.toString()));

        if (!application.getPhoneNumber().equals(authenticatedPhone)) {
            throw LoanApplicationException.unauthorized();
        }

        application.setStatus(ApplicationStatus.COMPLETED);
        loanApplicationRepository.save(application);
    }

    /**
     * Async method to push application to CRM.
     */
    @Async
    public void pushToCrmAsync(LoanApplication application) {
        try {
            crmClient.pushApplication(
                    application.getId().toString(),
                    application.getPhoneNumber(),
                    application.getFirstName(),
                    application.getLastName()).thenAccept(result -> {
                        if (result.success()) {
                            log.info("Application {} pushed to CRM, ref: {}",
                                    application.getId(), result.crmReferenceId());
                        } else {
                            log.warn("Failed to push application {} to CRM: {}",
                                    application.getId(), result.errorMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Error pushing to CRM for application: {}", application.getId(), e);
        }
    }

    /**
     * Synchronous method to process decision.
     */
    public void processDecision(LoanApplication application) {
        try {
            // Fetch CRM flags (now synchronous for speed)
            az.kredo.loan.integration.CRMClient.CustomerFlags flags = crmClient
                    .fetchCustomerFlags(application.getPhoneNumber()).join();

            log.info("CRM flags received for application: {}. Customer tier: {}",
                    application.getId(), flags.creditTier());

            // Evaluate using decision engine
            DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

            // Update application with decision
            application.setScore(result.getScore());
            application.setDecision(result.getDecision());
            application.setApprovedAmount(result.getApprovedAmount());
            application.setApr(result.getApr());
            application.setReasonCodes(result.getReasonCodes());

            // If rejected by bank, go straight to COMPLETED. If approved/review, wait for
            // customer accept.
            if (result.getDecision() == az.kredo.loan.entity.Decision.REJECTED) {
                application.setStatus(ApplicationStatus.COMPLETED);
            } else {
                application.setStatus(ApplicationStatus.OFFER_PENDING);
            }

            loanApplicationRepository.save(application);
            log.info("Application {} evaluation finished, status: {}",
                    application.getId(), application.getStatus());
        } catch (Exception e) {
            log.error("Error processing decision for application: {}", application.getId(), e);
        }
    }
}
