package ua.dymohlo.payment_service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.dymohlo.payment_service.client.LiqPayClient;
import ua.dymohlo.payment_service.dto.request.PaymentRequest;
import ua.dymohlo.payment_service.dto.response.PaymentResponse;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiqPayStrategy implements PaymentStrategy {

    private final LiqPayClient liqPayClient;

    @Value("${liqpay.result-url}")
    private String resultUrl;

    @Value("${liqpay.callback-url}")
    private String callbackUrl;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Sending card ****{} to LiqPay...", getCardMask(request.getCardNumber()));

        if (!validatePayment(request)) {
            return PaymentResponse.builder()
                    .success(false)
                    .message("Invalid payment data")
                    .build();
        }

        try {
            String orderId = generateOrderId();

            Map<String, Object> params = createPaymentParams(request, orderId);

            Map<String, Object> liqPayResponse = liqPayClient.processPayment(params);

            String status = getString(liqPayResponse, "status");
            String result = getString(liqPayResponse, "result");
            String errorCode = getString(liqPayResponse, "err_code");
            String errorDescription = getString(liqPayResponse, "err_description");
            String transactionId = getString(liqPayResponse, "transaction_id");

            log.info("LiqPay result: status={}, result={}, err_code={}", status, result, errorCode);

            boolean isSuccess = errorCode == null || errorCode.trim().isEmpty();

            if (isSuccess) {
                return PaymentResponse.builder()
                        .success(true)
                        .transactionId(transactionId)
                        .message("Payment successful")
                        .paymentData(liqPayResponse.toString())
                        .build();
            } else {
                return PaymentResponse.builder()
                        .success(false)
                        .transactionId(transactionId)
                        .message("Payment failed: " + (errorDescription != null ? errorDescription : errorCode))
                        .paymentData(liqPayResponse.toString())
                        .build();
            }

        } catch (Exception e) {
            log.error("Error calling LiqPay API", e);
            return PaymentResponse.builder()
                    .success(false)
                    .message("API call failed: " + e.getMessage())
                    .build();
        }
    }

    private Map<String, Object> createPaymentParams(PaymentRequest request, String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("action", "pay");
        params.put("amount", request.getAmount().toString());
        params.put("currency", request.getCurrency());
        params.put("description", "Payment for " + request.getSubscriptionName());
        params.put("order_id", orderId);

        params.put("card", normalizeCardNumber(request.getCardNumber()));
        String month = extractExpiryMonth(request.getCardExpiry());
        String year = extractExpiryYear(request.getCardExpiry());
        params.put("card_exp_month", month);
        params.put("card_exp_year", year.substring(2));
        params.put("card_date", month + year.substring(2));
        params.put("card_cvv", request.getCardCvv());

        params.put("result_url", resultUrl);
        params.put("server_url", callbackUrl);

        if (request.getUserEmail() != null) {
            params.put("sender_email", request.getUserEmail());
        }

        return params;
    }

    @Override
    public boolean validatePayment(PaymentRequest request) {
        return request.getAmount() != null &&
                request.getCurrency() != null &&
                request.getCardNumber() != null &&
                request.getCardExpiry() != null &&
                request.getCardCvv() != null;
    }

    private String normalizeCardNumber(String cardNumber) {
        return cardNumber.replaceAll("\\s+", "").replaceAll("-", "");
    }

    private String extractExpiryMonth(String expiry) {
        if (expiry.contains("/")) {
            return expiry.split("/")[0];
        }
        return expiry.substring(0, 2);
    }

    private String extractExpiryYear(String expiry) {
        if (expiry.contains("/")) {
            String year = expiry.split("/")[1];
            return year.length() == 2 ? "20" + year : year;
        }
        return "20" + expiry.substring(2, 4);
    }

    private String getCardMask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String normalized = normalizeCardNumber(cardNumber);
        return normalized.substring(Math.max(0, normalized.length() - 4));
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public String getPaymentType() {
        return "liqpay";
    }

    private String generateOrderId() {
        return "order_" + System.currentTimeMillis();
    }
}