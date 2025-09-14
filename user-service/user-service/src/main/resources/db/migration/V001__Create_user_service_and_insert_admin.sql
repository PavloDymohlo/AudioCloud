CREATE SCHEMA IF NOT EXISTS user_service;

CREATE TABLE IF NOT EXISTS user_service.users (
    uuid_id UUID PRIMARY KEY,
    auto_subscription VARCHAR(10) NOT NULL DEFAULT 'YES',
    subscription VARCHAR(100) NOT NULL DEFAULT 'FREE',
    subscription_expires_at TIMESTAMP WITH TIME ZONE,
    user_email VARCHAR(255) NOT NULL UNIQUE,
    user_role VARCHAR(50) NOT NULL,
    bank_card_number VARCHAR(20),
    bank_card_cvv VARCHAR(4),
    bank_card_expired VARCHAR(7)
);

INSERT INTO user_service.users (
    uuid_id,
    auto_subscription,
    subscription,
    subscription_expires_at,
    user_email,
    user_role,
    bank_card_number,
    bank_card_cvv,
    bank_card_expired
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'YES',
    'FREE',
    NULL,
    'admin@example.com',
    'ADMIN',
    '4242424242424242',
    '123',
    '12/27'
) ON CONFLICT (user_email) DO NOTHING;