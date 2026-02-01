-- Update constraints for new statuses and decisions
ALTER TABLE loan_applications DROP CONSTRAINT IF EXISTS chk_status;
ALTER TABLE loan_applications ADD CONSTRAINT chk_status CHECK (status IN (
    'OTP_PENDING', 
    'OTP_VERIFIED', 
    'INFO_SUBMITTED', 
    'AMOUNT_SUBMITTED', 
    'SCORING', 
    'OFFER_PENDING', 
    'OFFER_ACCEPTED', 
    'OFFER_REJECTED', 
    'PENDING_CRM', 
    'COMPLETED'
));

ALTER TABLE loan_applications DROP CONSTRAINT IF EXISTS chk_decision;
ALTER TABLE loan_applications ADD CONSTRAINT chk_decision CHECK (decision IS NULL OR decision IN (
    'APPROVED', 
    'REJECTED', 
    'MANUAL_REVIEW', 
    'PENDING', 
    'CUSTOMER_REJECTED'
));
