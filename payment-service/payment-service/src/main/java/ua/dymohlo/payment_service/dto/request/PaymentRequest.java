package ua.dymohlo.payment_service.dto.request;

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
    private BigDecimal amount;
    private String currency;
    private String subscriptionName;
    private String userEmail;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
}
