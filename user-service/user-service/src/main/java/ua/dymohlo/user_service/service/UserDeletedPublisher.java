package ua.dymohlo.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ua.dymohlo.user_service.dto.event.UserDeletedEvent;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDeletedPublisher {

    private final KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;

    public void publishUserDeletedEvent(String userEmail) {
        try {
            UserDeletedEvent event = UserDeletedEvent.builder()
                    .userEmail(userEmail)
                    .deletedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("user-deleted", userEmail, event);

            log.info("Published user deleted event: userEmail={}, deletedAt={}",
                    userEmail, event.getDeletedAt());

        } catch (Exception e) {
            log.error("Failed to publish user deleted event: userEmail={}", userEmail, e);
        }
    }
}