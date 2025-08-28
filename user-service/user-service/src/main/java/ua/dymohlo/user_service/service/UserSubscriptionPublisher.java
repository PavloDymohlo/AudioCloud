package ua.dymohlo.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ua.dymohlo.user_service.dto.event.UserSubscriptionEvent;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSubscriptionPublisher {

    private final KafkaTemplate<String, UserSubscriptionEvent> kafkaTemplate;

    @Value("${kafka.topics.user-subscriptions}")
    private String topicName;

    public void publishUserSubscriptionEvent(UUID userId, String subscription) {
        try {
            UserSubscriptionEvent event = UserSubscriptionEvent.builder()
                    .userId(userId)
                    .subscription(subscription)
                    .build();

            kafkaTemplate.send(topicName, userId.toString(), event);

            log.info("Published user subscription event: userId={}, subscription={}", userId, subscription);

        } catch (Exception e) {
            log.error("Failed to publish user subscription event: userId={}", userId, e);
        }
    }
}