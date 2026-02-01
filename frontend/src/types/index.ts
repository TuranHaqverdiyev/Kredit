// API Types for Kredo Loan Platform

// OTP Types
export interface GenerateOtpRequest {
    phoneNumber: string;
    channel: 'SMS' | 'EMAIL';
}

export interface GenerateOtpResponse {
    requestId: string;
    ttlSeconds: number;
}

export interface VerifyOtpRequest {
    phoneNumber: string;
    requestId: string;
    otpCode: string;
}

export interface VerifyOtpResponse {
    verified: boolean;
    accessToken: string;
    expiresInSeconds: number;
    personalData?: PersonalDataDto;
}

export interface PersonalDataDto {
    firstName: string;
    lastName: string;
    fin: string;
    dateOfBirth: string;
    address: string;
    employmentStatus: string;
    monthlyIncome: number;
    existingMonthlyDebt: number;
}

// Loan Application Types
export interface ConsentDto {
    termsAccepted: boolean;
    privacyAccepted: boolean;
}

export interface ApplyToLoanRequest {
    phoneNumber: string;
    firstName: string;
    lastName: string;
    fin: string;
    dateOfBirth: string;
    employmentStatus: EmploymentStatus;
    monthlyIncome: number;
    existingMonthlyDebt: number;
    address: string;
    consent: ConsentDto;
}

export interface ApplyToLoanResponse {
    applicationId: string;
    status: ApplicationStatus;
}

export interface SubmitAmountRequest {
    requestedAmount: number;
    termMonths: number;
}

export interface SubmitAmountResponse {
    applicationId: string;
    status: ApplicationStatus;
}

export interface LoanResultResponse {
    applicationId: string;
    status: ApplicationStatus;
    decision: Decision | null;
    score: number | null;
    approvedAmount: number | null;
    apr: number | null;
    reasonCodes: string[] | null;
    lastUpdated: string;
}

export type EmploymentStatus =
    | 'EMPLOYED'
    | 'SELF_EMPLOYED'
    | 'UNEMPLOYED'
    | 'RETIRED'
    | 'STUDENT';

export type ApplicationStatus =
    | 'OTP_PENDING'
    | 'OTP_VERIFIED'
    | 'INFO_SUBMITTED'
    | 'AMOUNT_SUBMITTED'
    | 'SCORING'
    | 'OFFER_PENDING'
    | 'OFFER_ACCEPTED'
    | 'OFFER_REJECTED'
    | 'PENDING_CRM'
    | 'COMPLETED';

export type Decision =
    | 'APPROVED'
    | 'REJECTED'
    | 'MANUAL_REVIEW'
    | 'PENDING'
    | 'CUSTOMER_REJECTED';

// Error Response
export interface ApiError {
    timestamp: string;
    path: string;
    errorCode: string;
    message: string;
    details?: Record<string, string>;
}

// Application State
export interface ApplicationState {
    step: number;
    phoneNumber: string;
    requestId: string | null;
    accessToken: string | null;
    applicationId: string | null;
    personalInfo: PersonalInfo | null;
    loanDetails: LoanDetails | null;
}

export interface PersonalInfo {
    firstName: string;
    lastName: string;
    fin: string;
    dateOfBirth: string;
    employmentStatus: EmploymentStatus;
    monthlyIncome: number;
    existingMonthlyDebt: number;
    address: string;
}

export interface LoanDetails {
    requestedAmount: number;
    termMonths: number;
}
