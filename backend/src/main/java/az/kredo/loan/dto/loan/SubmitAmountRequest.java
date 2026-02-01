package az.kredo.loan.dto.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to submit requested loan amount")
public class SubmitAmountRequest {

    @Schema(description = "Requested loan amount in AZN", example = "3000.00")
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "100.00", message = "Minimum loan amount is 100 AZN")
    @DecimalMax(value = "50000.00", message = "Maximum loan amount is 50,000 AZN")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal requestedAmount;

    @Schema(description = "Loan term in months", example = "12")
    @NotNull(message = "Term months is required")
    @Min(value = 3, message = "Minimum term is 3 months")
    @Max(value = 60, message = "Maximum term is 60 months")
    private Integer termMonths;
}
