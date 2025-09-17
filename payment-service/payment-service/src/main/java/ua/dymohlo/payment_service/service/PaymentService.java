package ua.dymohlo.payment_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.payment_service.dto.request.PaymentRequest;
import ua.dymohlo.payment_service.dto.request.RefundRequest;
import ua.dymohlo.payment_service.dto.response.PaymentResponse;
import ua.dymohlo.payment_service.strategy.PaymentStrategy;
import ua.dymohlo.payment_service.strategy.PaymentStrategyFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentStrategyFactory factory;

    public PaymentResponse processPayment(PaymentRequest request, String paymentType) {
        PaymentStrategy strategy = factory.getStrategy(paymentType);
        return strategy.processPayment(request);
    }

    public PaymentResponse processRefund(RefundRequest request) {
        PaymentStrategy strategy = factory.getStrategy("liqpay");
        return strategy.processRefund(request);
    }
}