package az.kredo.loan.exception;

public class LoanApplicationException extends BusinessException {
    public LoanApplicationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public static LoanApplicationException notFound(String applicationId) {
        return new LoanApplicationException("APPLICATION_NOT_FOUND",
                "Loan application not found: " + applicationId);
    }

    public static LoanApplicationException invalidStatus(String currentStatus, String expectedStatus) {
        return new LoanApplicationException("INVALID_STATUS",
                String.format("Application is in %s status, expected %s", currentStatus, expectedStatus));
    }

    public static LoanApplicationException phoneNotVerified() {
        return new LoanApplicationException("PHONE_NOT_VERIFIED",
                "Phone number must be verified before applying for a loan.");
    }

    public static LoanApplicationException duplicateApplication() {
        return new LoanApplicationException("DUPLICATE_APPLICATION",
                "An active loan application already exists for this phone number.");
    }

    public static LoanApplicationException unauthorized() {
        return new LoanApplicationException("UNAUTHORIZED",
                "You are not authorized to access this application.");
    }
}
