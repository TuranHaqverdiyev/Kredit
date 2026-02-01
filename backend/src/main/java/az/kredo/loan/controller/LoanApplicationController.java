package az.kredo.loan.controller;

import az.kredo.loan.dto.loan.*;
import az.kredo.loan.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/kredo-ms/loan-application")
@RequiredArgsConstructor
@Tag(name = "Loan Application Controller", description = "Loan application management")
@SecurityRequirement(name = "bearerAuth")
public class LoanApplicationController {

        private final LoanApplicationService loanApplicationService;

        @PostMapping("/apply-to-loan")
        @Operation(summary = "Apply for Loan", description = "Submit personal and financial information to apply for a loan")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Application submitted successfully", content = @Content(schema = @Schema(implementation = ApplyToLoanResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "409", description = "Duplicate application exists")
        })
        public ResponseEntity<ApplyToLoanResponse> applyToLoan(
                        @Valid @RequestBody ApplyToLoanRequest request,
                        Authentication authentication) {

                String authenticatedPhone = (String) authentication.getPrincipal();
                log.info("Loan application received");

                ApplyToLoanResponse response = loanApplicationService.applyToLoan(request, authenticatedPhone);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{applicationId}/submit-requested-amount")
        @Operation(summary = "Submit Requested Amount", description = "Submit the desired loan amount and term")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Amount submitted, scoring started", content = @Content(schema = @Schema(implementation = SubmitAmountResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Application not found"),
                        @ApiResponse(responseCode = "409", description = "Invalid application status")
        })
        public ResponseEntity<SubmitAmountResponse> submitRequestedAmount(
                        @PathVariable UUID applicationId,
                        @Valid @RequestBody SubmitAmountRequest request,
                        Authentication authentication) {

                String authenticatedPhone = (String) authentication.getPrincipal();
                log.info("Requested amount submission for application: {}", applicationId);

                SubmitAmountResponse response = loanApplicationService.submitRequestedAmount(
                                applicationId, request, authenticatedPhone);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{applicationId}/accept-offer")
        @Operation(summary = "Accept Loan Offer", description = "Accept the approved loan offer")
        public ResponseEntity<Void> acceptOffer(
                        @PathVariable UUID applicationId,
                        Authentication authentication) {
                String authenticatedPhone = (String) authentication.getPrincipal();
                loanApplicationService.acceptOffer(applicationId, authenticatedPhone);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/{applicationId}/reject-offer")
        @Operation(summary = "Reject Loan Offer", description = "Reject the approved loan offer")
        public ResponseEntity<Void> rejectOffer(
                        @PathVariable UUID applicationId,
                        Authentication authentication) {
                String authenticatedPhone = (String) authentication.getPrincipal();
                loanApplicationService.rejectOffer(applicationId, authenticatedPhone);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/{applicationId}/finalize")
        @Operation(summary = "Finalize Application", description = "Complete the application process after contract signature")
        public ResponseEntity<Void> finalizeApplication(
                        @PathVariable UUID applicationId,
                        Authentication authentication) {
                String authenticatedPhone = (String) authentication.getPrincipal();
                loanApplicationService.finalizeApplication(applicationId, authenticatedPhone);
                return ResponseEntity.ok().build();
        }

        @GetMapping("/{applicationId}/result")
        @Operation(summary = "Get Loan Result", description = "Get the decision result for a loan application")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Result retrieved", content = @Content(schema = @Schema(implementation = LoanResultResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Application not found")
        })
        public ResponseEntity<LoanResultResponse> getResult(
                        @PathVariable UUID applicationId,
                        Authentication authentication) {

                String authenticatedPhone = (String) authentication.getPrincipal();
                log.info("Result requested for application: {}", applicationId);

                LoanResultResponse response = loanApplicationService.getResult(applicationId, authenticatedPhone);
                return ResponseEntity.ok(response);
        }
}
