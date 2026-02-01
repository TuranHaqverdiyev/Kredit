import api from './api';
import type {
    GenerateOtpRequest,
    GenerateOtpResponse,
    VerifyOtpRequest,
    VerifyOtpResponse,
    ApplyToLoanRequest,
    ApplyToLoanResponse,
    SubmitAmountRequest,
    SubmitAmountResponse,
    LoanResultResponse,
} from '../types';

// OTP Service
export const otpService = {
    generateOtp: async (data: GenerateOtpRequest): Promise<GenerateOtpResponse> => {
        const response = await api.post<GenerateOtpResponse>('/otp-service/generate-otp', data);
        return response.data;
    },

    verifyOtp: async (data: VerifyOtpRequest): Promise<VerifyOtpResponse> => {
        const response = await api.post<VerifyOtpResponse>('/otp-service/verify-otp', data);
        return response.data;
    },
};

// Loan Application Service
export const loanService = {
    applyToLoan: async (data: ApplyToLoanRequest): Promise<ApplyToLoanResponse> => {
        const response = await api.post<ApplyToLoanResponse>('/loan-application/apply-to-loan', data);
        return response.data;
    },

    submitRequestedAmount: async (
        applicationId: string,
        data: SubmitAmountRequest
    ): Promise<SubmitAmountResponse> => {
        const response = await api.post<SubmitAmountResponse>(
            `/loan-application/${applicationId}/submit-requested-amount`,
            data
        );
        return response.data;
    },

    getResult: async (applicationId: string): Promise<LoanResultResponse> => {
        const response = await api.get<LoanResultResponse>(
            `/loan-application/${applicationId}/result`
        );
        return response.data;
    },

    acceptOffer: async (applicationId: string): Promise<void> => {
        await api.post(`/loan-application/${applicationId}/accept-offer`);
    },

    rejectOffer: async (applicationId: string): Promise<void> => {
        await api.post(`/loan-application/${applicationId}/reject-offer`);
    },

    finalizeApplication: async (applicationId: string): Promise<void> => {
        await api.post(`/loan-application/${applicationId}/finalize`);
    },
};
