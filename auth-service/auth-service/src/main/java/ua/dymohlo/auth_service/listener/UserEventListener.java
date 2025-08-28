package ua.dymohlo.auth_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ua.dymohlo.auth_service.dto.event.UserDeletedEvent;
import ua.dymohlo.auth_service.repository.UserRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

    private final UserRepository userRepository;

    @KafkaListener(topics = "user-deleted")
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received user deletion event for: {}", event.getUserEmail());

        userRepository.findByUserEmail(event.getUserEmail())
                .ifPresent(user -> {
                    userRepository.delete(user);
                    log.info("User deleted from auth service: {}", event.getUserEmail());
                });
    }
}