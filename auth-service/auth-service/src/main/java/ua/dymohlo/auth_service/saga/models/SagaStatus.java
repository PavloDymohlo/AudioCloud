package ua.dymohlo.auth_service.saga.models;

public enum SagaStatus {
    STARTED,
    IN_PROGRESS,
    COMPLETED,
    COMPENSATING,
    FAILED
}