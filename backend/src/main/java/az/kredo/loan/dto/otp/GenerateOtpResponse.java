package az.kredo.loan.dto.otp;

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
@Schema(description = "Response after OTP generation")
public class GenerateOtpResponse {

    @Schema(description = "Request ID to use for verification", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID requestId;

    @Schema(description = "Time-to-live for the OTP in seconds", example = "120")
    private int ttlSeconds;
}
