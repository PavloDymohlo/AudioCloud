CREATE SCHEMA IF NOT EXISTS subscription_service;

CREATE TABLE IF NOT EXISTS subscription_service.subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    subscription_name VARCHAR(255) NOT NULL UNIQUE,
    subscription_price DECIMAL(10,2) NOT NULL,
    subscription_duration_time INTEGER NOT NULL,
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'ENABLE'
);

-- Вставка даних з вашої таблиці
INSERT INTO subscription_service.subscription_plans
    (id, subscription_name, subscription_price, subscription_duration_time, subscription_status)
VALUES
    (2, 'FREE', 0, 0, 'ENABLE'),
    (1, 'TRIAL', 0, 100, 'ENABLE'),
    (3, 'OPTIMAL', 100, 100, 'ENABLE'),
    (4, 'MAXIMUM', 300, 100, 'ENABLE')
ON CONFLICT (subscription_name) DO NOTHING;

SELECT setval('subscription_service.subscription_plans_id_seq', 4, true);