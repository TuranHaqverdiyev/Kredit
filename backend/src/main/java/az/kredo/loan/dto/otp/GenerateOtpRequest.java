package az.kredo.loan.dto.otp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate a new OTP")
public class GenerateOtpRequest {

    @Schema(description = "Phone number in E.164 format", example = "+994501234567")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "Phone number must be in format +994XXXXXXXXX")
    private String phoneNumber;

    @Schema(description = "Delivery channel", example = "SMS", allowableValues = { "SMS", "EMAIL" })
    @NotBlank(message = "Channel is required")
    @Pattern(regexp = "^(SMS|EMAIL)$", message = "Channel must be SMS or EMAIL")
    @Builder.Default
    private String channel = "SMS";
}
