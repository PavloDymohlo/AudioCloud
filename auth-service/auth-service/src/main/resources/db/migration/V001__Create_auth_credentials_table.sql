CREATE SCHEMA IF NOT EXISTS auth_service;

CREATE TABLE auth_service.auth_credentials (
    id UUID PRIMARY KEY,
    user_email VARCHAR(255) UNIQUE NOT NULL,
    user_password VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    user_created_at TIMESTAMP NOT NULL,
    user_last_login TIMESTAMP
);

INSERT INTO auth_service.auth_credentials (
    id,
    user_email,
    user_password,
    user_role,
    user_created_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@example.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'ADMIN',
    CURRENT_TIMESTAMP
);