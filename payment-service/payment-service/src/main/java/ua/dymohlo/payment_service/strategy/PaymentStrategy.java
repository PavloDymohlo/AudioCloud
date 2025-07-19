package ua.dymohlo.payment_service.strategy;

import ua.dymohlo.payment_service.dto.request.PaymentRequest;
import ua.dymohlo.payment_service.dto.response.PaymentResponse;

public interface PaymentStrategy {
    PaymentResponse processPayment(PaymentRequest request);
    boolean validatePayment(PaymentRequest request);
    String getPaymentType();
}
