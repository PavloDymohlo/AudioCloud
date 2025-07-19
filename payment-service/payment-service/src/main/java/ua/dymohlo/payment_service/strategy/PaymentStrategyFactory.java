package ua.dymohlo.payment_service.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {
    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::getPaymentType,
                        Function.identity()
                ));
    }

    public PaymentStrategy getStrategy(String paymentType) {
        return strategies.get(paymentType);
    }
}