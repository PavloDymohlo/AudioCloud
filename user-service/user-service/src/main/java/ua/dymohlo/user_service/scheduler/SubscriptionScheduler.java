package ua.dymohlo.user_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.dymohlo.user_service.entity.User;
import ua.dymohlo.user_service.handler.UserExpiredSubscriptionHandler;
import ua.dymohlo.user_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionScheduler {
    private final UserRepository userRepository;

    private final UserExpiredSubscriptionHandler userExpiredSubscriptionHandler;
    private static final List<String> EXCLUDED_ROLES = new ArrayList<>(List.of("ADMIN"));
    private static final List<String> EXCLUDED_SUBSCRIPTIONS = new ArrayList<>(List.of("FREE"));


    @Scheduled(fixedRate = 6000)
    public void checkSubscriptionExpiration() {
        List<User> expiredUsers = userRepository.findExpiredUsers(
                LocalDateTime.now(),
                EXCLUDED_ROLES,
                EXCLUDED_SUBSCRIPTIONS
        );

        userExpiredSubscriptionHandler.checkUserAutoRenewStatus(expiredUsers);
    }
}
