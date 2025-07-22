package ua.dymohlo.auth_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String userEmail;
    private String subscriptionName;
    private BigDecimal amount;
    private String currency;
    private String cardNumber;
    private String cardCvv;
    private String cardExpiry;
    private String paymentProvider;
}
