package az.kredo.loan.dto.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Loan application result with decision")
public class LoanResultResponse {

    @Schema(description = "Application ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID applicationId;

    @Schema(description = "Application status", example = "COMPLETED", allowableValues = { "INFO_SUBMITTED",
            "AMOUNT_SUBMITTED", "SCORING", "PENDING_CRM", "COMPLETED" })
    private String status;

    @Schema(description = "Credit decision", example = "APPROVED", allowableValues = { "APPROVED", "REJECTED",
            "MANUAL_REVIEW", "PENDING" })
    private String decision;

    @Schema(description = "Credit score (300-850)", example = "720")
    private Integer score;

    @Schema(description = "Approved loan amount in AZN", example = "2500.00")
    private BigDecimal approvedAmount;

    @Schema(description = "Annual percentage rate", example = "18.5")
    private BigDecimal apr;

    @Schema(description = "Decision reason codes", example = "[\"DTI_OK\", \"INCOME_OK\"]")
    private List<String> reasonCodes;

    @Schema(description = "Last update timestamp", example = "2026-02-01T12:00:00Z")
    private Instant lastUpdated;
}
