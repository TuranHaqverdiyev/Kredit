package az.kredo.loan.service;

import az.kredo.loan.dto.otp.GenerateOtpRequest;
import az.kredo.loan.dto.otp.GenerateOtpResponse;
import az.kredo.loan.dto.otp.VerifyOtpRequest;
import az.kredo.loan.dto.otp.VerifyOtpResponse;
import az.kredo.loan.entity.OtpRequest;
import az.kredo.loan.exception.OtpException;
import az.kredo.loan.repository.OtpRequestRepository;
import az.kredo.loan.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRequestRepository otpRequestRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private OtpService otpService;

    private static final String TEST_PHONE = "+994501234567";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(otpService, "otpTtlSeconds", 120);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 5);
        ReflectionTestUtils.setField(otpService, "lockoutMinutes", 5);
        ReflectionTestUtils.setField(otpService, "codeLength", 6);
    }

    @Test
    @DisplayName("Generate OTP should create and save OTP request")
    void generateOtp_shouldCreateOtpRequest() {
        // Given
        GenerateOtpRequest request = GenerateOtpRequest.builder()
                .phoneNumber(TEST_PHONE)
                .channel("SMS")
                .build();

        when(otpRequestRepository.save(any(OtpRequest.class)))
                .thenAnswer(inv -> {
                    OtpRequest saved = inv.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
                    return saved;
                });

        // When
        GenerateOtpResponse response = otpService.generateOtp(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).isNotNull();
        assertThat(response.getTtlSeconds()).isEqualTo(120);

        ArgumentCaptor<OtpRequest> captor = ArgumentCaptor.forClass(OtpRequest.class);
        verify(otpRequestRepository).save(captor.capture());

        OtpRequest saved = captor.getValue();
        assertThat(saved.getPhoneNumber()).isEqualTo(TEST_PHONE);
        assertThat(saved.getChannel()).isEqualTo("SMS");
        assertThat(saved.getOtpHash()).isNotBlank();
        assertThat(saved.getAttempts()).isEqualTo(0);
        assertThat(saved.getVerified()).isFalse();
    }

    @Test
    @DisplayName("Verify OTP with correct code should return token")
    void verifyOtp_withCorrectCode_shouldReturnToken() {
        // Given
        UUID requestId = UUID.randomUUID();
        String otpCode = "123456";
        String hashedOtp = passwordEncoder.encode(otpCode);

        OtpRequest otpRequest = OtpRequest.builder()
                .id(requestId)
                .phoneNumber(TEST_PHONE)
                .otpHash(hashedOtp)
                .attempts(0)
                .verified(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(2, ChronoUnit.MINUTES))
                .build();

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber(TEST_PHONE)
                .requestId(requestId)
                .otpCode(otpCode)
                .build();

        when(otpRequestRepository.findById(requestId)).thenReturn(Optional.of(otpRequest));
        when(jwtTokenProvider.generateToken(TEST_PHONE)).thenReturn("test.jwt.token");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(900);
        when(otpRequestRepository.save(any())).thenReturn(otpRequest);

        // When
        VerifyOtpResponse response = otpService.verifyOtp(request);

        // Then
        assertThat(response.isVerified()).isTrue();
        assertThat(response.getAccessToken()).isEqualTo("test.jwt.token");
        assertThat(response.getExpiresInSeconds()).isEqualTo(900);
        assertThat(otpRequest.getVerified()).isTrue();
    }

    @Test
    @DisplayName("Verify OTP with wrong code should throw exception")
    void verifyOtp_withWrongCode_shouldThrowException() {
        // Given
        UUID requestId = UUID.randomUUID();
        String correctOtp = "123456";
        String wrongOtp = "654321";
        String hashedOtp = passwordEncoder.encode(correctOtp);

        OtpRequest otpRequest = OtpRequest.builder()
                .id(requestId)
                .phoneNumber(TEST_PHONE)
                .otpHash(hashedOtp)
                .attempts(0)
                .verified(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(2, ChronoUnit.MINUTES))
                .build();

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber(TEST_PHONE)
                .requestId(requestId)
                .otpCode(wrongOtp)
                .build();

        when(otpRequestRepository.findById(requestId)).thenReturn(Optional.of(otpRequest));
        when(otpRequestRepository.save(any())).thenReturn(otpRequest);

        // When/Then
        assertThatThrownBy(() -> otpService.verifyOtp(request))
                .isInstanceOf(OtpException.class)
                .hasFieldOrPropertyWithValue("errorCode", "OTP_INVALID");
    }

    @Test
    @DisplayName("Verify OTP when expired should throw exception")
    void verifyOtp_whenExpired_shouldThrowException() {
        // Given
        UUID requestId = UUID.randomUUID();
        String otpCode = "123456";
        String hashedOtp = passwordEncoder.encode(otpCode);

        OtpRequest otpRequest = OtpRequest.builder()
                .id(requestId)
                .phoneNumber(TEST_PHONE)
                .otpHash(hashedOtp)
                .attempts(0)
                .verified(false)
                .createdAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .expiresAt(Instant.now().minus(3, ChronoUnit.MINUTES)) // Expired
                .build();

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber(TEST_PHONE)
                .requestId(requestId)
                .otpCode(otpCode)
                .build();

        when(otpRequestRepository.findById(requestId)).thenReturn(Optional.of(otpRequest));

        // When/Then
        assertThatThrownBy(() -> otpService.verifyOtp(request))
                .isInstanceOf(OtpException.class)
                .hasFieldOrPropertyWithValue("errorCode", "OTP_EXPIRED");
    }

    @Test
    @DisplayName("Verify OTP exceeding max attempts should lock out")
    void verifyOtp_exceedingMaxAttempts_shouldLockOut() {
        // Given
        UUID requestId = UUID.randomUUID();
        String wrongOtp = "654321";
        String hashedOtp = passwordEncoder.encode("123456");

        OtpRequest otpRequest = OtpRequest.builder()
                .id(requestId)
                .phoneNumber(TEST_PHONE)
                .otpHash(hashedOtp)
                .attempts(5) // Already at max
                .verified(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(2, ChronoUnit.MINUTES))
                .build();

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber(TEST_PHONE)
                .requestId(requestId)
                .otpCode(wrongOtp)
                .build();

        when(otpRequestRepository.findById(requestId)).thenReturn(Optional.of(otpRequest));
        when(otpRequestRepository.save(any())).thenReturn(otpRequest);

        // When/Then
        assertThatThrownBy(() -> otpService.verifyOtp(request))
                .isInstanceOf(OtpException.class)
                .hasFieldOrPropertyWithValue("errorCode", "OTP_MAX_ATTEMPTS");

        assertThat(otpRequest.getLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("Verify OTP when locked should throw exception")
    void verifyOtp_whenLocked_shouldThrowException() {
        // Given
        UUID requestId = UUID.randomUUID();
        String otpCode = "123456";
        String hashedOtp = passwordEncoder.encode(otpCode);

        OtpRequest otpRequest = OtpRequest.builder()
                .id(requestId)
                .phoneNumber(TEST_PHONE)
                .otpHash(hashedOtp)
                .attempts(5)
                .verified(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(2, ChronoUnit.MINUTES))
                .lockedUntil(Instant.now().plus(5, ChronoUnit.MINUTES)) // Locked
                .build();

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber(TEST_PHONE)
                .requestId(requestId)
                .otpCode(otpCode)
                .build();

        when(otpRequestRepository.findById(requestId)).thenReturn(Optional.of(otpRequest));

        // When/Then
        assertThatThrownBy(() -> otpService.verifyOtp(request))
                .isInstanceOf(OtpException.class)
                .hasFieldOrPropertyWithValue("errorCode", "OTP_LOCKED");
    }

    @Test
    @DisplayName("Verify OTP not found should throw exception")
    void verifyOtp_notFound_shouldThrowException() {
        // Given
        UUID requestId = UUID.randomUUID();
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber(TEST_PHONE)
                .requestId(requestId)
                .otpCode("123456")
                .build();

        when(otpRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> otpService.verifyOtp(request))
                .isInstanceOf(OtpException.class)
                .hasFieldOrPropertyWithValue("errorCode", "OTP_NOT_FOUND");
    }
}
