package ua.dymohlo.auth_service.saga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ua.dymohlo.auth_service.saga.dto.RefundRequest;
import ua.dymohlo.auth_service.saga.entity.SagaState;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompensationHandler {

    private final WebClient webClient;

    public boolean compensatePayment(SagaState sagaState) {
        if (sagaState.getPaymentTransactionId() == null) {
            log.info("No payment to compensate for SAGA: {}", sagaState.getSagaId());
            return true;
        }

        try {
            log.info("Starting payment compensation for SAGA: {}, transactionId: {}",
                    sagaState.getSagaId(), sagaState.getPaymentTransactionId());

            String response = webClient.post()
                    .uri("lb://payment-service/api/v1/payment/refund")
                    .header("Content-Type", "application/json")
                    .bodyValue(createRefundRequest(sagaState))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Payment compensation successful for SAGA: {}, response: {}",
                    sagaState.getSagaId(), response);
            return true;

        } catch (Exception e) {
            log.error("Payment compensation failed for SAGA: {}, error: {}",
                    sagaState.getSagaId(), e.getMessage());
            return false;
        }
    }

    public boolean compensateUser(SagaState sagaState) {
        try {
            log.info("Starting user compensation for SAGA: {}, userId: {}",
                    sagaState.getSagaId(), sagaState.getUserId());

            String response = webClient.delete()
                    .uri("lb://user-service/api/v1/users/saga/{userId}", sagaState.getUserId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("User compensation successful for SAGA: {}, response: {}",
                    sagaState.getSagaId(), response);
            return true;

        } catch (Exception e) {
            log.error("User compensation failed for SAGA: {}, error: {}",
                    sagaState.getSagaId(), e.getMessage());
            return false;
        }
    }

    private RefundRequest createRefundRequest(SagaState sagaState) {
        return RefundRequest.builder()
                .transactionId(sagaState.getPaymentTransactionId())
                .userEmail(sagaState.getUserEmail())
                .reason("SAGA_COMPENSATION")
                .build();
    }
}