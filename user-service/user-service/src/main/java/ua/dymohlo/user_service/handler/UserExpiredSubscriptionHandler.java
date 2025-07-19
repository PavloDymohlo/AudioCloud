package ua.dymohlo.user_service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.dymohlo.user_service.entity.User;
import ua.dymohlo.user_service.models.AutoSubscriptionStatus;
import ua.dymohlo.user_service.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserExpiredSubscriptionHandler {
    private final UserRepository userRepository;
    private static final String DEFAULT_SUBSCRIPTION = "FREE";

    public void checkUserAutoRenewStatus(List<User> users) {
        Map<Boolean, List<User>> partitionedUsers = users.parallelStream()
                .collect(Collectors.partitioningBy(user ->
                        user.getAutoSubscription() == AutoSubscriptionStatus.YES
                ));
        List<User> usersWithAutoRenew = partitionedUsers.get(true);
        List<User> usersWithoutAutoRenew = partitionedUsers.get(false);

        resetUserSubscription(usersWithoutAutoRenew);
    }

    private void resetUserSubscription(List<User> users) {
        users.parallelStream()
                .forEach(user -> {
                    user.setSubscription(DEFAULT_SUBSCRIPTION);
                    userRepository.save(user);
                });
    }

    private void renewUserSubscription(List<User> users){
        /**TO DO LATER*/
    }
}
