package az.kredo.loan.controller;

import az.kredo.loan.dto.otp.GenerateOtpRequest;
import az.kredo.loan.dto.otp.GenerateOtpResponse;
import az.kredo.loan.dto.otp.VerifyOtpRequest;
import az.kredo.loan.dto.otp.VerifyOtpResponse;
import az.kredo.loan.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/kredo-ms/otp-service")
@RequiredArgsConstructor
@Tag(name = "OTP Controller", description = "Phone verification via OTP")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/generate-otp")
    @Operation(summary = "Generate OTP", description = "Generate a new OTP for phone verification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP generated successfully", content = @Content(schema = @Schema(implementation = GenerateOtpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<GenerateOtpResponse> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
        log.info("OTP generation requested for channel: {}", request.getChannel());
        GenerateOtpResponse response = otpService.generateOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP and receive access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP verified successfully", content = @Content(schema = @Schema(implementation = VerifyOtpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid OTP or expired"),
            @ApiResponse(responseCode = "429", description = "Too many attempts")
    })
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification requested for requestId: {}", request.getRequestId());
        VerifyOtpResponse response = otpService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }
}
