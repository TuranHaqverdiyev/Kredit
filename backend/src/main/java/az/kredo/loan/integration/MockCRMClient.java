package az.kredo.loan.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Mock CRM client that simulates real CRM behavior with artificial delays.
 * In production, replace this with actual CRM integration.
 */
@Slf4j
@Component
public class MockCRMClient implements CRMClient {

    private static final int MIN_DELAY_MS = 50;
    private static final int MAX_DELAY_MS = 150;

    @Override
    public CompletableFuture<PushResult> pushApplication(String applicationId, String phoneNumber,
            String firstName, String lastName) {
        log.info("Mock CRM: Pushing application {} to CRM", applicationId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate network delay
                int delay = ThreadLocalRandom.current().nextInt(MIN_DELAY_MS, MAX_DELAY_MS + 1);
                TimeUnit.MILLISECONDS.sleep(delay);

                // Generate a mock CRM reference ID
                String crmReferenceId = "CRM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                log.info("Mock CRM: Application {} pushed successfully, CRM ref: {}",
                        applicationId, crmReferenceId);

                return PushResult.success(crmReferenceId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Mock CRM: Push interrupted for application {}", applicationId);
                return PushResult.failure("CRM push interrupted");
            }
        });
    }

    @Override
    public CompletableFuture<CustomerFlags> fetchCustomerFlags(String phoneNumber) {
        log.info("Mock CRM: Fetching customer flags for phone [MASKED]");

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate network delay
                int delay = ThreadLocalRandom.current().nextInt(MIN_DELAY_MS, MAX_DELAY_MS + 1);
                TimeUnit.MILLISECONDS.sleep(delay);

                // Deterministic mock response based on phone number hash
                int phoneHash = Math.abs(phoneNumber.hashCode());

                boolean existingCustomer = (phoneHash % 3) == 0;
                boolean hasActiveLoans = existingCustomer && (phoneHash % 5) == 0;
                boolean hasDefaultHistory = (phoneHash % 17) == 0;
                int creditTier = existingCustomer ? (phoneHash % 5) + 1 : 0;

                List<String> specialPrograms;
                if (existingCustomer && (phoneHash % 7) == 0) {
                    specialPrograms = List.of("LOYALTY_DISCOUNT", "FAST_TRACK");
                } else if (existingCustomer) {
                    specialPrograms = List.of("STANDARD");
                } else {
                    specialPrograms = List.of();
                }

                log.info("Mock CRM: Customer flags fetched - existing: {}, activeLoans: {}, tier: {}",
                        existingCustomer, hasActiveLoans, creditTier);

                return new CustomerFlags(existingCustomer, hasActiveLoans, hasDefaultHistory,
                        creditTier, specialPrograms);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Mock CRM: Fetch interrupted for phone [MASKED]");
                return CustomerFlags.newCustomer();
            }
        });
    }
}
