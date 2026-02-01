package az.kredo.loan.repository;

import az.kredo.loan.entity.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRequestRepository extends JpaRepository<OtpRequest, UUID> {

    /**
     * Find the most recent non-expired, non-verified OTP request for a phone number
     */
    @Query("SELECT o FROM OtpRequest o WHERE o.phoneNumber = :phoneNumber " +
            "AND o.verified = false AND o.expiresAt > :now " +
            "ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpRequest> findLatestActiveByPhoneNumber(String phoneNumber, Instant now);

    /**
     * Find OTP request by ID
     */
    Optional<OtpRequest> findById(UUID id);

    /**
     * Delete expired OTP requests (for cleanup job)
     */
    void deleteByExpiresAtBefore(Instant before);
}
