import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { ApiError } from '../types';

// Token storage - using memory for security (not localStorage)
let accessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
    accessToken = token;
};

export const getAccessToken = () => accessToken;

export const clearAccessToken = () => {
    accessToken = null;
};

// Create axios instance
const api = axios.create({
    baseURL: '/api/v1/kredo-ms',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 30000,
});

// Request interceptor to add auth token
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // Generate request ID for tracing
        config.headers['X-Request-Id'] = crypto.randomUUID();

        // Add auth token if available
        if (accessToken && config.url && !config.url.includes('/otp-service/')) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    (error: AxiosError<ApiError>) => {
        if (error.response) {
            const apiError = error.response.data;

            // Handle specific error codes
            if (error.response.status === 401) {
                clearAccessToken();
                // Optionally redirect to login
            }

            return Promise.reject(apiError);
        }

        // Network error
        return Promise.reject({
            errorCode: 'NETWORK_ERROR',
            message: 'Network error. Please check your connection.',
        });
    }
);

export default api;
