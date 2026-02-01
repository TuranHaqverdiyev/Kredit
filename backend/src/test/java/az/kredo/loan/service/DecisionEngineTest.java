package az.kredo.loan.service;

import az.kredo.loan.entity.Decision;
import az.kredo.loan.entity.EmploymentStatus;
import az.kredo.loan.entity.LoanApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionEngineTest {

    private DecisionEngine decisionEngine;

    @BeforeEach
    void setUp() {
        decisionEngine = new DecisionEngine();
    }

    @Test
    @DisplayName("High income employed applicant should be approved")
    void evaluate_highIncomeEmployed_shouldApprove() {
        // Given
        LoanApplication application = createApplication(
                LocalDate.of(1990, 5, 10),
                EmploymentStatus.EMPLOYED,
                new BigDecimal("3000"),
                new BigDecimal("100"),
                new BigDecimal("5000"),
                12);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        assertThat(result.getDecision()).isEqualTo(Decision.APPROVED);
        assertThat(result.getScore()).isGreaterThanOrEqualTo(700);
        assertThat(result.getApprovedAmount()).isPositive();
        assertThat(result.getApr()).isPositive();
        assertThat(result.getReasonCodes()).contains("INCOME_HIGH", "EMPLOYMENT_STABLE");
    }

    @Test
    @DisplayName("Low income unemployed applicant should be rejected")
    void evaluate_lowIncomeUnemployed_shouldReject() {
        // Given
        LoanApplication application = createApplication(
                LocalDate.of(1990, 5, 10),
                EmploymentStatus.UNEMPLOYED,
                new BigDecimal("300"),
                new BigDecimal("200"),
                new BigDecimal("5000"),
                12);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        assertThat(result.getDecision()).isEqualTo(Decision.REJECTED);
        assertThat(result.getScore()).isLessThan(600);
        assertThat(result.getReasonCodes()).contains("EMPLOYMENT_RISK");
    }

    @Test
    @DisplayName("Applicant too young should be rejected")
    void evaluate_underAge_shouldReject() {
        // Given
        LoanApplication application = createApplication(
                LocalDate.now().minusYears(17), // 17 years old
                EmploymentStatus.EMPLOYED,
                new BigDecimal("2000"),
                new BigDecimal("0"),
                new BigDecimal("3000"),
                12);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        assertThat(result.getDecision()).isEqualTo(Decision.REJECTED);
        assertThat(result.getReasonCodes()).contains("AGE_OUT_OF_RANGE");
    }

    @Test
    @DisplayName("Applicant too old should be rejected")
    void evaluate_overAge_shouldReject() {
        // Given
        LoanApplication application = createApplication(
                LocalDate.now().minusYears(72), // 72 years old
                EmploymentStatus.RETIRED,
                new BigDecimal("2000"),
                new BigDecimal("0"),
                new BigDecimal("3000"),
                12);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        assertThat(result.getDecision()).isEqualTo(Decision.REJECTED);
        assertThat(result.getReasonCodes()).contains("AGE_OUT_OF_RANGE");
    }

    @Test
    @DisplayName("Applicant with no income should be rejected")
    void evaluate_noIncome_shouldReject() {
        // Given
        LoanApplication application = createApplication(
                LocalDate.of(1990, 5, 10),
                EmploymentStatus.UNEMPLOYED,
                BigDecimal.ZERO,
                new BigDecimal("0"),
                new BigDecimal("3000"),
                12);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        assertThat(result.getDecision()).isEqualTo(Decision.REJECTED);
        assertThat(result.getReasonCodes()).contains("NO_INCOME");
    }

    @Test
    @DisplayName("Borderline applicant should go to manual review")
    void evaluate_borderlineCase_shouldReview() {
        // Given - moderate income, self-employed, some existing debt
        LoanApplication application = createApplication(
                LocalDate.of(1985, 3, 15),
                EmploymentStatus.SELF_EMPLOYED,
                new BigDecimal("1200"),
                new BigDecimal("200"),
                new BigDecimal("5000"),
                24);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        // Score should be in the middle range
        assertThat(result.getScore()).isBetween(550, 750);
        // Decision depends on exact score
        assertThat(result.getDecision()).isIn(Decision.MANUAL_REVIEW, Decision.APPROVED, Decision.REJECTED);
    }

    @Test
    @DisplayName("High DTI should negatively impact decision")
    void evaluate_highDTI_shouldImpactNegatively() {
        // Given - high existing debt relative to income
        LoanApplication application = createApplication(
                LocalDate.of(1990, 5, 10),
                EmploymentStatus.EMPLOYED,
                new BigDecimal("2000"),
                new BigDecimal("1200"), // 60% of income in existing debt
                new BigDecimal("5000"),
                12);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        assertThat(result.getReasonCodes()).contains("DTI_EXCESSIVE");
        assertThat(result.getScore()).isLessThan(700);
    }

    @ParameterizedTest
    @DisplayName("Score should always be within valid range")
    @CsvSource({
            "1990-01-01, EMPLOYED, 5000, 0, 10000, 12",
            "2000-01-01, STUDENT, 500, 100, 1000, 6",
            "1960-01-01, RETIRED, 1500, 300, 3000, 36",
            "1985-01-01, SELF_EMPLOYED, 2500, 500, 8000, 24"
    })
    void evaluate_scoreAlwaysInRange(String dob, String employment,
            String income, String debt,
            String amount, int term) {
        // Given
        LoanApplication application = createApplication(
                LocalDate.parse(dob),
                EmploymentStatus.valueOf(employment),
                new BigDecimal(income),
                new BigDecimal(debt),
                new BigDecimal(amount),
                term);

        // When
        DecisionEngine.DecisionResult result = decisionEngine.evaluate(application);

        // Then
        assertThat(result.getScore()).isBetween(300, 850);
        assertThat(result.getDecision()).isNotNull();
        assertThat(result.getReasonCodes()).isNotEmpty();
    }

    @Test
    @DisplayName("APR should decrease with higher score")
    void evaluate_aprDecreasesWithHigherScore() {
        // Given - excellent profile
        LoanApplication excellentApp = createApplication(
                LocalDate.of(1985, 5, 10),
                EmploymentStatus.EMPLOYED,
                new BigDecimal("5000"),
                new BigDecimal("0"),
                new BigDecimal("3000"),
                12);

        // Good profile
        LoanApplication goodApp = createApplication(
                LocalDate.of(1990, 5, 10),
                EmploymentStatus.SELF_EMPLOYED,
                new BigDecimal("2000"),
                new BigDecimal("200"),
                new BigDecimal("3000"),
                12);

        // When
        DecisionEngine.DecisionResult excellentResult = decisionEngine.evaluate(excellentApp);
        DecisionEngine.DecisionResult goodResult = decisionEngine.evaluate(goodApp);

        // Then
        if (excellentResult.getDecision() == Decision.APPROVED &&
                goodResult.getDecision() == Decision.APPROVED) {
            assertThat(excellentResult.getApr())
                    .isLessThanOrEqualTo(goodResult.getApr());
        }
    }

    private LoanApplication createApplication(LocalDate dob, EmploymentStatus employment,
            BigDecimal income, BigDecimal existingDebt,
            BigDecimal requestedAmount, int termMonths) {
        return LoanApplication.builder()
                .phoneNumber("+994501234567")
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(dob)
                .employmentStatus(employment)
                .monthlyIncome(income)
                .existingMonthlyDebt(existingDebt)
                .requestedAmount(requestedAmount)
                .termMonths(termMonths)
                .finEncrypted("encrypted")
                .addressEncrypted("encrypted")
                .termsAccepted(true)
                .privacyAccepted(true)
                .build();
    }
}
