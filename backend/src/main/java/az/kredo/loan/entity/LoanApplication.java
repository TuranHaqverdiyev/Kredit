package az.kredo.loan.entity;

import az.kredo.loan.security.FieldEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "fin_encrypted", nullable = false)
    private String finEncrypted;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "employment_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus;

    @Column(name = "monthly_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "existing_monthly_debt", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal existingMonthlyDebt = BigDecimal.ZERO;

    @Column(name = "address_encrypted", nullable = false)
    private String addressEncrypted;

    @Column(name = "terms_accepted", nullable = false)
    @Builder.Default
    private Boolean termsAccepted = false;

    @Column(name = "privacy_accepted", nullable = false)
    @Builder.Default
    private Boolean privacyAccepted = false;

    @Column(name = "consent_timestamp")
    private Instant consentTimestamp;

    // Loan request details
    @Column(name = "requested_amount", precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "term_months")
    private Integer termMonths;

    // Decision results
    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.INFO_SUBMITTED;

    @Column(name = "score")
    private Integer score;

    @Column(name = "decision", length = 20)
    @Enumerated(EnumType.STRING)
    private Decision decision;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "apr", precision = 5, scale = 2)
    private BigDecimal apr;

    @Column(name = "reason_codes")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Builder.Default
    private List<String> reasonCodes = new ArrayList<>();

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Transient field to hold decrypted FIN
    @Transient
    private String fin;

    // Transient field to hold decrypted address
    @Transient
    private String address;

    /**
     * Encrypts and stores the FIN
     */
    public void setFin(String fin, FieldEncryptor encryptor) {
        this.fin = fin;
        this.finEncrypted = encryptor.encrypt(fin);
    }

    /**
     * Decrypts and returns the FIN
     */
    public String getFin(FieldEncryptor encryptor) {
        if (this.fin == null && this.finEncrypted != null) {
            this.fin = encryptor.decrypt(this.finEncrypted);
        }
        return this.fin;
    }

    /**
     * Encrypts and stores the address
     */
    public void setAddress(String address, FieldEncryptor encryptor) {
        this.address = address;
        this.addressEncrypted = encryptor.encrypt(address);
    }

    /**
     * Decrypts and returns the address
     */
    public String getAddress(FieldEncryptor encryptor) {
        if (this.address == null && this.addressEncrypted != null) {
            this.address = encryptor.decrypt(this.addressEncrypted);
        }
        return this.address;
    }

    public int getAge() {
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
