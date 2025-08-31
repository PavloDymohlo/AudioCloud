package ua.dymohlo.auth_service.saga.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.dymohlo.auth_service.saga.models.SagaStatus;
import ua.dymohlo.auth_service.saga.models.SagaStep;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saga_states", schema = "auth_service")
public class SagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sagaId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStep currentStep;

    @Column(length = 1000)
    private String paymentTransactionId;

    @Column(length = 1000)
    private String subscriptionName;

    @Column(length = 2000)
    private String errorMessage;

    @Column(length = 5000)
    private String requestData;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private Integer retryCount;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markCompleted() {
        completedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        if (retryCount == null) {
            retryCount = 0;
        }
        retryCount++;
        updatedAt = LocalDateTime.now();
    }
}