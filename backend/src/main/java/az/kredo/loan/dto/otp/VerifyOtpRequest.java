package az.kredo.loan.dto.otp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to verify an OTP")
public class VerifyOtpRequest {

    @Schema(description = "Phone number in E.164 format", example = "+994501234567")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "Phone number must be in format +994XXXXXXXXX")
    private String phoneNumber;

    @Schema(description = "Request ID from generate-otp response", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "Request ID is required")
    private UUID requestId;

    @Schema(description = "6-digit OTP code", example = "123456")
    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP code must be exactly 6 digits")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP code must be 6 digits")
    private String otpCode;
}
