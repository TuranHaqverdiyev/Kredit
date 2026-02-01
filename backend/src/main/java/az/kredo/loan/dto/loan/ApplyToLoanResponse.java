package az.kredo.loan.dto.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after loan application submission")
public class ApplyToLoanResponse {

    @Schema(description = "Application ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID applicationId;

    @Schema(description = "Application status", example = "INFO_SUBMITTED")
    private String status;
}
