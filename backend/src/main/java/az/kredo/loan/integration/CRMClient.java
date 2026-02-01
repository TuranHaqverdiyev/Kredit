package az.kredo.loan.integration;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for CRM integration.
 * Designed for async processing to avoid blocking the main request flow.
 */
public interface CRMClient {

    /**
     * Push a loan application to the CRM system.
     * 
     * @param applicationId The loan application ID
     * @param phoneNumber   Customer phone number
     * @param firstName     Customer first name
     * @param lastName      Customer last name
     * @return Result of the push operation
     */
    CompletableFuture<PushResult> pushApplication(String applicationId, String phoneNumber,
            String firstName, String lastName);

    /**
     * Fetch customer flags from the CRM system.
     * 
     * @param phoneNumber Customer phone number
     * @return Customer flags that may affect loan decisions
     */
    CompletableFuture<CustomerFlags> fetchCustomerFlags(String phoneNumber);

    /**
     * Result of pushing an application to CRM.
     */
    record PushResult(boolean success, String crmReferenceId, String errorMessage) {
        public static PushResult success(String crmReferenceId) {
            return new PushResult(true, crmReferenceId, null);
        }

        public static PushResult failure(String errorMessage) {
            return new PushResult(false, null, errorMessage);
        }
    }

    /**
     * Customer flags from CRM that affect loan decisions.
     */
    record CustomerFlags(
            boolean existingCustomer,
            boolean hasActiveLoans,
            boolean hasDefaultHistory,
            int creditTier,
            List<String> specialPrograms) {
        public static CustomerFlags newCustomer() {
            return new CustomerFlags(false, false, false, 0, List.of());
        }
    }
}
