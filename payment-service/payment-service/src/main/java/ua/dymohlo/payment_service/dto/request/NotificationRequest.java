package ua.dymohlo.payment_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private boolean success;
    private String transactionId;
    private String message;
    private String paymentUrl;
    private String paymentData;
    private String paymentSignature;
    private String recipient;
    private String notificationType;
    private String messageType;
}