CREATE SCHEMA IF NOT EXISTS music_content_service;

CREATE TABLE IF NOT EXISTS music_content_service.music_files (
    id BIGSERIAL PRIMARY KEY,
    music_file_name VARCHAR(255) NOT NULL,
    music_file_path VARCHAR(500) NOT NULL,
    subscription_type VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_music_files_name
ON music_content_service.music_files(music_file_name);

CREATE INDEX IF NOT EXISTS idx_music_files_subscription
ON music_content_service.music_files(subscription_type);