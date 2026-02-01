package az.kredo.loan.exception;

public class OtpException extends BusinessException {
    public OtpException(String errorCode, String message) {
        super(errorCode, message);
    }

    public static OtpException expired() {
        return new OtpException("OTP_EXPIRED", "OTP has expired. Please request a new one.");
    }

    public static OtpException invalid() {
        return new OtpException("OTP_INVALID", "Invalid OTP code.");
    }

    public static OtpException maxAttemptsExceeded() {
        return new OtpException("OTP_MAX_ATTEMPTS",
                "Maximum OTP verification attempts exceeded. Please wait 5 minutes.");
    }

    public static OtpException locked() {
        return new OtpException("OTP_LOCKED", "Too many failed attempts. Please wait before trying again.");
    }

    public static OtpException notFound() {
        return new OtpException("OTP_NOT_FOUND", "OTP request not found. Please request a new OTP.");
    }
}
