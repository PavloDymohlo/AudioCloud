CREATE TABLE IF NOT EXISTS auth_service.saga_states (
    saga_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_step VARCHAR(50) NOT NULL,
    payment_transaction_id VARCHAR(1000),
    subscription_name VARCHAR(1000),
    error_message VARCHAR(2000),
    request_data VARCHAR(5000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0
);

CREATE INDEX idx_saga_states_user_email ON auth_service.saga_states(user_email);
CREATE INDEX idx_saga_states_status ON auth_service.saga_states(status);
CREATE INDEX idx_saga_states_created_at ON auth_service.saga_states(created_at);
CREATE INDEX idx_saga_states_status_updated_at ON auth_service.saga_states(status, updated_at);