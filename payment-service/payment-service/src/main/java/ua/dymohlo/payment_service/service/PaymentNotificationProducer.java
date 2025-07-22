package ua.dymohlo.payment_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ua.dymohlo.payment_service.dto.request.NotificationRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationProducer {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${kafka.topics.notifications}")
    private String topicName;

    public void sendPaymentNotification(NotificationRequest notification) {
        log.info("Sending payment notification for transaction: {}", notification.getTransactionId());
        kafkaTemplate.send(topicName, notification.getTransactionId(), notification);
    }
}