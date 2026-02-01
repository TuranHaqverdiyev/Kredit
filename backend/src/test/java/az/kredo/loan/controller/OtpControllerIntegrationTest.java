package az.kredo.loan.controller;

import az.kredo.loan.dto.otp.GenerateOtpRequest;
import az.kredo.loan.dto.otp.VerifyOtpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OtpControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/kredo-ms/otp-service";

    @Test
    @DisplayName("Generate OTP should return request ID and TTL")
    void generateOtp_shouldReturnRequestIdAndTtl() throws Exception {
        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .phoneNumber("+994501234567")
                .channel("SMS")
                .build();

        mockMvc.perform(post(BASE_URL + "/generate-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.ttlSeconds").value(120));
    }

    @Test
    @DisplayName("Generate OTP with invalid phone should return validation error")
    void generateOtp_invalidPhone_shouldReturnError() throws Exception {
        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .phoneNumber("invalid-phone")
                .channel("SMS")
                .build();

        mockMvc.perform(post(BASE_URL + "/generate-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.phoneNumber").isNotEmpty());
    }

    @Test
    @DisplayName("Verify OTP with non-existent request ID should return not found")
    void verifyOtp_nonExistentRequestId_shouldReturnNotFound() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber("+994501234567")
                .requestId(UUID.randomUUID())
                .otpCode("123456")
                .build();

        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("OTP_NOT_FOUND"));
    }

    @Test
    @DisplayName("Verify OTP with missing fields should return validation error")
    void verifyOtp_missingFields_shouldReturnError() throws Exception {
        String invalidRequest = "{\"phoneNumber\":\"+994501234567\"}";

        mockMvc.perform(post(BASE_URL + "/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Generate OTP should include X-Request-Id header in response")
    void generateOtp_shouldIncludeCorrelationId() throws Exception {
        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .phoneNumber("+994501234567")
                .channel("SMS")
                .build();

        mockMvc.perform(post(BASE_URL + "/generate-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-Id", "test-correlation-id")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "test-correlation-id"));
    }
}
