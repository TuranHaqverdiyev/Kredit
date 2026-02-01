package az.kredo.loan.repository;

import az.kredo.loan.entity.ApplicationStatus;
import az.kredo.loan.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    /**
     * Find loan application by ID
     */
    Optional<LoanApplication> findById(UUID id);

    /**
     * Find all applications for a phone number
     */
    List<LoanApplication> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    /**
     * Check if there's an active (non-completed) application for a phone number
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN TRUE ELSE FALSE END " +
            "FROM LoanApplication l WHERE l.phoneNumber = :phoneNumber " +
            "AND l.status NOT IN ('COMPLETED')")
    boolean hasActiveApplication(String phoneNumber);

    /**
     * Find applications by status (for batch processing)
     */
    List<LoanApplication> findByStatus(ApplicationStatus status);

    /**
     * Find applications pending CRM sync
     */
    List<LoanApplication> findByStatusIn(List<ApplicationStatus> statuses);
}
