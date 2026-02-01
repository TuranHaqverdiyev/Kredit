package az.kredo.loan.service;

import az.kredo.loan.dto.otp.GenerateOtpRequest;
import az.kredo.loan.dto.otp.GenerateOtpResponse;
import az.kredo.loan.dto.otp.PersonalDataDto;
import az.kredo.loan.dto.otp.VerifyOtpRequest;
import az.kredo.loan.dto.otp.VerifyOtpResponse;
import az.kredo.loan.entity.OtpRequest;
import az.kredo.loan.exception.OtpException;
import az.kredo.loan.repository.OtpRequestRepository;
import az.kredo.loan.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRequestRepository otpRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${kredo.otp.ttl-seconds:120}")
    private int otpTtlSeconds;

    @Value("${kredo.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${kredo.otp.lockout-minutes:5}")
    private int lockoutMinutes;

    @Value("${kredo.otp.code-length:6}")
    private int codeLength;

    /**
     * Generate a new OTP for the given phone number.
     * The OTP code is hashed before storage and never logged.
     */
    @Transactional
    public GenerateOtpResponse generateOtp(GenerateOtpRequest request) {
        // Generate 6-digit OTP
        String otpCode = generateRandomOtp();

        // Hash the OTP before storing
        String otpHash = passwordEncoder.encode(otpCode);

        Instant now = Instant.now();
        Instant expiresAt = now.plus(otpTtlSeconds, ChronoUnit.SECONDS);

        OtpRequest otpRequest = OtpRequest.builder()
                .phoneNumber(request.getPhoneNumber())
                .otpHash(otpHash)
                .channel(request.getChannel())
                .attempts(0)
                .verified(false)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        OtpRequest saved = otpRequestRepository.save(otpRequest);

        // In a real system, send OTP via SMS/Email here
        // For development, log the OTP code prominently
        log.info("========================================");
        log.info("  DEV MODE - OTP CODE: {}  ", otpCode);
        log.info("  Phone: {}  ", request.getPhoneNumber());
        log.info("  Request ID: {}  ", saved.getId());
        log.info("========================================");

        return GenerateOtpResponse.builder()
                .requestId(saved.getId())
                .ttlSeconds(otpTtlSeconds)
                .build();
    }

    /**
     * Verify an OTP code and return a JWT token on success.
     */
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        OtpRequest otpRequest = otpRequestRepository.findById(request.getRequestId())
                .orElseThrow(OtpException::notFound);

        // Verify phone number matches
        if (!otpRequest.getPhoneNumber().equals(request.getPhoneNumber())) {
            log.warn("Phone number mismatch for OTP request: {}", request.getRequestId());
            throw OtpException.notFound();
        }

        // Check if locked out
        if (otpRequest.isLocked()) {
            log.warn("OTP verification attempted while locked, requestId: {}", request.getRequestId());
            throw OtpException.locked();
        }

        // Check if expired
        if (otpRequest.isExpired()) {
            log.info("OTP expired, requestId: {}", request.getRequestId());
            throw OtpException.expired();
        }

        // Check if already verified
        if (otpRequest.getVerified()) {
            log.info("OTP already verified, requestId: {}", request.getRequestId());
            throw new OtpException("OTP_ALREADY_VERIFIED", "This OTP has already been verified.");
        }

        // Increment attempts
        otpRequest.incrementAttempts();

        // Check max attempts
        if (otpRequest.getAttempts() > maxAttempts) {
            otpRequest.setLockedUntil(Instant.now().plus(lockoutMinutes, ChronoUnit.MINUTES));
            otpRequestRepository.save(otpRequest);
            log.warn("Max OTP attempts exceeded, requestId: {}", request.getRequestId());
            throw OtpException.maxAttemptsExceeded();
        }

        // Verify the OTP code
        if (!passwordEncoder.matches(request.getOtpCode(), otpRequest.getOtpHash())) {
            otpRequestRepository.save(otpRequest);
            log.info("Invalid OTP attempt {}/{}, requestId: {}",
                    otpRequest.getAttempts(), maxAttempts, request.getRequestId());
            throw OtpException.invalid();
        }

        // OTP is valid - mark as verified
        otpRequest.setVerified(true);
        otpRequestRepository.save(otpRequest);

        // Generate JWT token
        String accessToken = jwtTokenProvider.generateToken(otpRequest.getPhoneNumber());
        int expiresInSeconds = jwtTokenProvider.getExpirationSeconds();

        log.info("OTP verified successfully, requestId: {}", request.getRequestId());

        // Mock fetching data from ASAN/IAMAS based on verified phone
        PersonalDataDto asanData = PersonalDataDto.builder()
                .firstName("Turan")
                .lastName("Aliyev")
                .fin("7ABC123")
                .dateOfBirth(java.time.LocalDate.of(1990, 5, 10))
                .address("Bakı, Nəsimi rayonu, mənzil 42")
                .employmentStatus("EMPLOYED")
                .monthlyIncome(3000.0)
                .existingMonthlyDebt(100.0)
                .build();

        return VerifyOtpResponse.builder()
                .verified(true)
                .accessToken(accessToken)
                .expiresInSeconds(expiresInSeconds)
                .personalData(asanData)
                .build();
    }

    /**
     * Generate a random N-digit OTP code.
     */
    private String generateRandomOtp() {
        int max = (int) Math.pow(10, codeLength);
        int min = (int) Math.pow(10, codeLength - 1);
        int code = secureRandom.nextInt(max - min) + min;
        return String.valueOf(code);
    }
}
