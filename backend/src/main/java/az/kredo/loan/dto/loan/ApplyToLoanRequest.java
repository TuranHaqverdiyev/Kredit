package az.kredo.loan.dto.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to apply for a loan")
public class ApplyToLoanRequest {

    @Schema(description = "Phone number in E.164 format", example = "+994501234567")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "Phone number must be in format +994XXXXXXXXX")
    private String phoneNumber;

    @Schema(description = "First name", example = "Turan")
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Aliyev")
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Schema(description = "Personal identification number (FIN)", example = "AZE1234567")
    @NotBlank(message = "FIN is required")
    @Pattern(regexp = "^[A-Z0-9]{7,10}$", message = "FIN must be 7-10 alphanumeric characters")
    private String fin;

    @Schema(description = "Date of birth", example = "1999-05-10")
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Schema(description = "Employment status", example = "EMPLOYED", allowableValues = { "EMPLOYED", "SELF_EMPLOYED",
            "UNEMPLOYED", "RETIRED", "STUDENT" })
    @NotBlank(message = "Employment status is required")
    @Pattern(regexp = "^(EMPLOYED|SELF_EMPLOYED|UNEMPLOYED|RETIRED|STUDENT)$", message = "Invalid employment status")
    private String employmentStatus;

    @Schema(description = "Monthly income in AZN", example = "1200.00")
    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid income format")
    private BigDecimal monthlyIncome;

    @Schema(description = "Existing monthly debt payments in AZN", example = "150.00")
    @NotNull(message = "Existing monthly debt is required")
    @DecimalMin(value = "0.00", message = "Existing monthly debt cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid debt format")
    @Builder.Default
    private BigDecimal existingMonthlyDebt = BigDecimal.ZERO;

    @Schema(description = "Residential address", example = "Baku, Azerbaijan")
    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    private String address;

    @Schema(description = "Consent information")
    @NotNull(message = "Consent information is required")
    @Valid
    private ConsentDto consent;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User consent information")
    public static class ConsentDto {

        @Schema(description = "Whether terms and conditions are accepted", example = "true")
        @AssertTrue(message = "Terms and conditions must be accepted")
        private boolean termsAccepted;

        @Schema(description = "Whether privacy policy is accepted", example = "true")
        @AssertTrue(message = "Privacy policy must be accepted")
        private boolean privacyAccepted;
    }
}
