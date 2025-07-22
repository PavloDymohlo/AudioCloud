package ua.dymohlo.auth_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ua.dymohlo.auth_service.dto.request.PaymentRequest;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.dto.response.PaymentResultResponse;
import ua.dymohlo.auth_service.dto.response.SubscriptionResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentClient {

    private final SubscriptionClient subscriptionClient;
    private final WebClient webClient;
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

    public PaymentResultResponse paymentProcess(RegisterRequest request) {
        try {
            SubscriptionResponse subscription = getSubscriptionData();
            PaymentRequest paymentRequest = buildPaymentRequest(request, subscription);
            String paymentResponse = sendPaymentRequest(paymentRequest);
            return parsePaymentResult(paymentResponse);

        } catch (Exception e) {
            log.error("Error processing payment for user: {}", request.getUserEmail(), e);
            throw new PaymentProcessingException("Failed to process payment", e);
        }
    }

    private SubscriptionResponse getSubscriptionData() {
        try {
            String subscriptionJson = subscriptionClient.getDefaultSubscription();
            return objectMapper.readValue(subscriptionJson, SubscriptionResponse.class);
        } catch (Exception e) {
            log.error("Error parsing subscription response: {}", e.getMessage());
            throw new SubscriptionParsingException("Failed to parse subscription data", e);
        }
    }

    private PaymentRequest buildPaymentRequest(RegisterRequest request, SubscriptionResponse subscription) {
        return PaymentRequest.builder()
                .userEmail(request.getUserEmail())
                .subscriptionName(subscription.getSubscriptionName())
                .amount(subscription.getSubscriptionPrice())
                .currency(defaultCurrency)
                .cardNumber(request.getBankCardNumber())
                .cardCvv(request.getBankCardNumberCVV())
                .cardExpiry(request.getBankCardNumberExpired())
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

    private PaymentResultResponse parsePaymentResult(String paymentResponse) {
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

    public static class SubscriptionParsingException extends RuntimeException {
        public SubscriptionParsingException(String message, Throwable cause) {
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