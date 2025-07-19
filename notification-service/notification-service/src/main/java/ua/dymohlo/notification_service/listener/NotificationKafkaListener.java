package ua.dymohlo.notification_service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ua.dymohlo.notification_service.dto.request.NotificationRequest;
import ua.dymohlo.notification_service.strategy.NotificationStrategy;
import ua.dymohlo.notification_service.strategy.NotificationStrategyFactory;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationKafkaListener {
    private final ObjectMapper objectMapper;
    private final NotificationStrategyFactory factory;

    @KafkaListener(topics = "notifications")
    public void handleNotification(String message) {
        NotificationRequest request = null;
        try {
            request = objectMapper.readValue(message, NotificationRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String notificationType = request.getNotificationType();

        NotificationStrategy strategy = factory.getStrategy(notificationType);
        strategy.sendNotification(request);
    }
}