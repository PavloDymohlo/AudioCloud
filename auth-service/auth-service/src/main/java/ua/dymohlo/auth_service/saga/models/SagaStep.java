package ua.dymohlo.auth_service.saga.models;

public enum SagaStep {
    STARTED,
    SUBSCRIPTION_CHECK,
    PAYMENT_PROCESSING,
    USER_CREATION,
    COMPLETED,


    COMPENSATING_PAYMENT,
    COMPENSATING_USER,
    COMPENSATION_COMPLETED
}