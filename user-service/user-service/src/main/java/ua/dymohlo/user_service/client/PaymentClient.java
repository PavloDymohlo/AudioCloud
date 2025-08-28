package ua.dymohlo.user_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ua.dymohlo.user_service.dto.request.PaymentRequest;
import ua.dymohlo.user_service.dto.response.PaymentResultResponse;
import ua.dymohlo.user_service.dto.response.SubscriptionResponse;
import ua.dymohlo.user_service.entity.User;

import static ua.dymohlo.user_service.constants.SubscriptionConstants.DEFAULT_SUBSCRIPTION;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentClient {

    private final SubscriptionClient subscriptionClient;
    private final WebClient webClient;
    private User user;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payment.service-name}")
    private String serviceName;

    @Value("${payment.api.base-path}")
    private String basePath;

    @Value("${payment.api.process-endpoint}")
    private String processEndpoint;

    @Value("${payment.default-currency:UAH}")
    private String defaultCurrency;

    @Value("${payment.default-provider}")
    private String defaultProvider;

    @Value("${payment.scheme:http}")
    private String scheme;

    public boolean paymentProcess(User user, SubscriptionResponse subscription) {
        if (subscription.getSubscriptionName().equals(DEFAULT_SUBSCRIPTION)) {
            return true;
        }
        try {
            PaymentRequest paymentRequest = buildPaymentRequest(user, subscription);
            String paymentResponse = sendPaymentRequest(paymentRequest);
            log.info("response: " + paymentResponse);
            return parsePaymentResult(paymentResponse, subscription.getSubscriptionName()).isSuccess();

        } catch (Exception e) {
            log.error("Error processing payment for user: {}", user.getUserEmail(), e);
            throw new PaymentProcessingException("Failed to process payment", e);
        }
    }


    private PaymentRequest buildPaymentRequest(User user, SubscriptionResponse subscription) {
        return PaymentRequest.builder()
                .userEmail(user.getUserEmail())
                .subscriptionName(subscription.getSubscriptionName())
                .amount(subscription.getSubscriptionPrice())
                .currency(defaultCurrency)
                .cardNumber(user.getBankCardNumber())
                .cardCvv(user.getBankCardCvv())
                .cardExpiry(user.getBankCardExpired())
                .build();
    }

    private String sendPaymentRequest(PaymentRequest paymentRequest) {
        String url = buildPaymentUrl();

        log.info("Sending payment request to: {}", url);
        log.info("Processing payment for subscription: {}, user: {}",
                paymentRequest.getSubscriptionName(), paymentRequest.getUserEmail());

        try {
            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(paymentRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Payment service response received");
            return response;

        } catch (Exception e) {
            log.error("Error connecting to payment service: {}", e.getMessage());
            throw new PaymentServiceConnectionException("Failed to connect to payment service", e);
        }
    }

    private String buildPaymentUrl() {
        return String.format("%s://%s%s%s/%s",
                scheme, serviceName, basePath, processEndpoint, defaultProvider);
    }

    private PaymentResultResponse parsePaymentResult(String paymentResponse, String subscriptionName) {
        try {
            JsonNode responseNode = objectMapper.readTree(paymentResponse);
            boolean success = responseNode.path("success").asBoolean(false);
            String message = responseNode.path("message").asText("Unknown error");
            String transactionId = responseNode.path("transactionId").asText(null);

            if (success) {
                log.info("Payment processed successfully. Transaction ID: {}", transactionId);
            } else {
                log.warn("Payment failed: {}", message);
            }

            return PaymentResultResponse.builder()
                    .success(success)
                    .message(message)
                    .subscriptionName(subscriptionName)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing payment response: {}", e.getMessage());
            throw new PaymentResponseParsingException("Failed to parse payment response", e);
        }
    }

    public static class PaymentProcessingException extends RuntimeException {
        public PaymentProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PaymentServiceConnectionException extends RuntimeException {
        public PaymentServiceConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PaymentResponseParsingException extends RuntimeException {
        public PaymentResponseParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}