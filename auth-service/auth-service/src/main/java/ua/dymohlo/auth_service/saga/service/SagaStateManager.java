package ua.dymohlo.auth_service.saga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.dymohlo.auth_service.saga.entity.SagaState;
import ua.dymohlo.auth_service.saga.models.SagaStatus;
import ua.dymohlo.auth_service.saga.models.SagaStep;
import ua.dymohlo.auth_service.saga.repository.SagaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaStateManager {

    private final SagaRepository sagaRepository;

    @Transactional
    public SagaState createSaga(UUID userId, String userEmail, String requestData) {
        SagaState sagaState = SagaState.builder()
                .userId(userId)
                .userEmail(userEmail)
                .status(SagaStatus.STARTED)
                .currentStep(SagaStep.STARTED)
                .requestData(requestData)
                .retryCount(0)
                .build();

        SagaState savedState = sagaRepository.save(sagaState);
        log.info("Created SAGA: sagaId={}, userId={}, userEmail={}",
                savedState.getSagaId(), userId, userEmail);

        return savedState;
    }

    @Transactional
    public SagaState updateStep(UUID sagaId, SagaStep step, SagaStatus status) {
        Optional<SagaState> optionalSaga = sagaRepository.findById(sagaId);
        if (optionalSaga.isEmpty()) {
            throw new RuntimeException("SAGA not found: " + sagaId);
        }

        SagaState sagaState = optionalSaga.get();
        sagaState.setCurrentStep(step);
        sagaState.setStatus(status);

        if (status == SagaStatus.COMPLETED || status == SagaStatus.FAILED) {
            sagaState.markCompleted();
        }

        SagaState savedState = sagaRepository.save(sagaState);
        log.info("Updated SAGA: sagaId={}, step={}, status={}", sagaId, step, status);

        return savedState;
    }

    @Transactional
    public SagaState updateWithData(UUID sagaId, SagaStep step, SagaStatus status,
                                    String paymentTransactionId, String subscriptionName) {
        Optional<SagaState> optionalSaga = sagaRepository.findById(sagaId);
        if (optionalSaga.isEmpty()) {
            throw new RuntimeException("SAGA not found: " + sagaId);
        }

        SagaState sagaState = optionalSaga.get();
        sagaState.setCurrentStep(step);
        sagaState.setStatus(status);

        if (paymentTransactionId != null) {
            sagaState.setPaymentTransactionId(paymentTransactionId);
        }
        if (subscriptionName != null) {
            sagaState.setSubscriptionName(subscriptionName);
        }

        return sagaRepository.save(sagaState);
    }

    @Transactional
    public SagaState markFailed(UUID sagaId, String errorMessage) {
        Optional<SagaState> optionalSaga = sagaRepository.findById(sagaId);
        if (optionalSaga.isEmpty()) {
            throw new RuntimeException("SAGA not found: " + sagaId);
        }

        SagaState sagaState = optionalSaga.get();
        sagaState.setStatus(SagaStatus.FAILED);
        sagaState.setErrorMessage(errorMessage);
        sagaState.markCompleted();

        SagaState savedState = sagaRepository.save(sagaState);
        log.error("SAGA failed: sagaId={}, error={}", sagaId, errorMessage);

        return savedState;
    }

    @Transactional
    public SagaState incrementRetryCount(UUID sagaId) {
        Optional<SagaState> optionalSaga = sagaRepository.findById(sagaId);
        if (optionalSaga.isEmpty()) {
            throw new RuntimeException("SAGA not found: " + sagaId);
        }

        SagaState sagaState = optionalSaga.get();
        sagaState.incrementRetryCount();

        return sagaRepository.save(sagaState);
    }

    public Optional<SagaState> getSaga(UUID sagaId) {
        return sagaRepository.findById(sagaId);
    }

    public boolean isUserRegistrationInProgress(String userEmail) {
        List<SagaStatus> activeStatuses = List.of(
                SagaStatus.STARTED,
                SagaStatus.IN_PROGRESS,
                SagaStatus.COMPENSATING
        );
        return sagaRepository.existsByUserEmailAndStatusIn(userEmail, activeStatuses);
    }

    public List<SagaState> getStuckSagas(int timeoutMinutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<SagaStatus> activeStatuses = List.of(SagaStatus.STARTED, SagaStatus.IN_PROGRESS);
        return sagaRepository.findStuckSagas(activeStatuses, cutoffTime);
    }
}