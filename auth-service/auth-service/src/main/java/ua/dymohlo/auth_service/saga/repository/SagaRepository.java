package ua.dymohlo.auth_service.saga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.dymohlo.auth_service.saga.entity.SagaState;
import ua.dymohlo.auth_service.saga.models.SagaStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaRepository extends JpaRepository<SagaState, UUID> {

    Optional<SagaState> findByUserEmail(String userEmail);

    List<SagaState> findByStatus(SagaStatus status);

    @Query("SELECT s FROM SagaState s WHERE s.status IN :statuses AND s.updatedAt < :before")
    List<SagaState> findStuckSagas(@Param("statuses") List<SagaStatus> statuses,
                                   @Param("before") LocalDateTime before);

    @Query("SELECT s FROM SagaState s WHERE s.status = :status AND s.retryCount < :maxRetries")
    List<SagaState> findRetriableSagas(@Param("status") SagaStatus status,
                                       @Param("maxRetries") Integer maxRetries);

    List<SagaState> findByStatusAndCreatedAtBefore(SagaStatus status, LocalDateTime before);

    boolean existsByUserEmailAndStatusIn(String userEmail, List<SagaStatus> statuses);
}