package az.kredo.loan.dto.otp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after OTP verification")
public class VerifyOtpResponse {

    @Schema(description = "Whether the OTP was verified successfully", example = "true")
    private boolean verified;

    @Schema(description = "JWT access token for authenticated requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Token expiration time in seconds", example = "900")
    private int expiresInSeconds;

    @Schema(description = "Personal data fetched from ASAN (IAMAS)")
    private PersonalDataDto personalData;
}
