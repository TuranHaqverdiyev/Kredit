-- Kredo Loan Platform - Initial Schema
-- V1: OTP Requests and Loan Applications tables

-- OTP Requests table for phone verification
CREATE TABLE otp_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(20) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'SMS',
    attempts INTEGER NOT NULL DEFAULT 0,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_until TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT chk_channel CHECK (channel IN ('SMS', 'EMAIL'))
);

CREATE INDEX idx_otp_phone_number ON otp_requests(phone_number);
CREATE INDEX idx_otp_expires_at ON otp_requests(expires_at);

-- Loan Applications table
CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    fin_encrypted TEXT NOT NULL,
    date_of_birth DATE NOT NULL,
    employment_status VARCHAR(20) NOT NULL,
    monthly_income DECIMAL(15, 2) NOT NULL,
    existing_monthly_debt DECIMAL(15, 2) NOT NULL DEFAULT 0,
    address_encrypted TEXT NOT NULL,
    terms_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    privacy_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    consent_timestamp TIMESTAMP WITH TIME ZONE,
    
    -- Loan request details
    requested_amount DECIMAL(15, 2),
    term_months INTEGER,
    
    -- Decision results
    status VARCHAR(30) NOT NULL DEFAULT 'INFO_SUBMITTED',
    score INTEGER,
    decision VARCHAR(20),
    approved_amount DECIMAL(15, 2),
    apr DECIMAL(5, 2),
    reason_codes TEXT[],
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_employment_status CHECK (employment_status IN ('EMPLOYED', 'SELF_EMPLOYED', 'UNEMPLOYED', 'RETIRED', 'STUDENT')),
    CONSTRAINT chk_status CHECK (status IN ('OTP_PENDING', 'OTP_VERIFIED', 'INFO_SUBMITTED', 'AMOUNT_SUBMITTED', 'SCORING', 'PENDING_CRM', 'COMPLETED')),
    CONSTRAINT chk_decision CHECK (decision IS NULL OR decision IN ('APPROVED', 'REJECTED', 'MANUAL_REVIEW', 'PENDING'))
);

CREATE INDEX idx_loan_app_phone ON loan_applications(phone_number);
CREATE INDEX idx_loan_app_status ON loan_applications(status);
CREATE INDEX idx_loan_app_created ON loan_applications(created_at);

-- Audit log table for compliance
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(100),
    changes JSONB,
    ip_address VARCHAR(45),
    request_id VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for loan_applications
CREATE TRIGGER update_loan_applications_updated_at
    BEFORE UPDATE ON loan_applications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
