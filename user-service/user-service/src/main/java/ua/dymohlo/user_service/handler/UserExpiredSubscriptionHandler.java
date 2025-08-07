package ua.dymohlo.user_service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.dymohlo.user_service.client.PaymentClient;
import ua.dymohlo.user_service.client.SubscriptionClient;
import ua.dymohlo.user_service.dto.response.SubscriptionResponse;
import ua.dymohlo.user_service.entity.User;
import ua.dymohlo.user_service.models.AutoSubscriptionStatus;
import ua.dymohlo.user_service.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserExpiredSubscriptionHandler {
    private final UserRepository userRepository;
    private final PaymentClient paymentClient;
    private final SubscriptionClient subscriptionClient;
    private static final String DEFAULT_SUBSCRIPTION = "FREE";
    private static final String TRIAL_SUBSCRIPTION = "TRIAL";
    private static final String AFTER_TRIAL_SUBSCRIPTION = "MAXIMUM";

    public void checkUserAutoRenewStatus(List<User> users) {
        Map<Boolean, List<User>> partitionedUsers = users.parallelStream()
                .collect(Collectors.partitioningBy(user ->
                        user.getAutoSubscription() == AutoSubscriptionStatus.YES
                ));
        List<User> usersWithAutoRenew = partitionedUsers.get(true);
        List<User> usersWithoutAutoRenew = partitionedUsers.get(false);
        if (!usersWithAutoRenew.isEmpty()) {
            renewUserSubscription(usersWithAutoRenew);
        }

        if (!usersWithoutAutoRenew.isEmpty()) {
            resetUserSubscription(usersWithoutAutoRenew);
        }
    }

    private void resetUserSubscription(List<User> users) {
        users.parallelStream()
                .forEach(user -> {
                    user.setSubscription(DEFAULT_SUBSCRIPTION);
                    userRepository.save(user);
                });
    }

    private void renewUserSubscription(List<User> users) {
        Map<String, SubscriptionResponse> subscriptionMap = loadSubscriptionsMap();

        if (subscriptionMap.isEmpty()) {
            log.warn("No subscriptions available, skipping renewal process");
            return;
        }

        users.parallelStream().forEach(user -> processUserRenewal(user, subscriptionMap));
    }

    private Map<String, SubscriptionResponse> loadSubscriptionsMap() {
        log.info("Loading subscriptions from subscription-service");

        try {
            List<SubscriptionResponse> subscriptions = subscriptionClient.getAllSubscriptions();

            Map<String, SubscriptionResponse> subscriptionMap = subscriptions.stream()
                    .collect(Collectors.toMap(
                            SubscriptionResponse::getSubscriptionName,
                            subscription -> subscription
                    ));

            log.info("Loaded {} subscription types: {}",
                    subscriptionMap.size(), subscriptionMap.keySet());

            return subscriptionMap;

        } catch (Exception e) {
            log.error("Failed to load subscriptions: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    private void processUserRenewal(User user, Map<String, SubscriptionResponse> subscriptionMap) {
        try {
            String subscriptionForPayment = determineSubscriptionForPayment(user);
            SubscriptionResponse paymentSubscriptionData = getSubscriptionData(subscriptionForPayment, subscriptionMap);

            if (paymentSubscriptionData == null) {
                log.warn("Subscription '{}' not found for user {}", subscriptionForPayment, user.getUserEmail());
                return;
            }

           // boolean paymentSuccess = processPayment(user, paymentSubscriptionData);
            boolean paymentSuccess = paymentClient.paymentProcess(user, paymentSubscriptionData);
            if (paymentSuccess) {
                handleSuccessfulPayment(user, subscriptionForPayment, paymentSubscriptionData);
            } else {
                handleFailedPayment(user);
            }

        } catch (Exception e) {
            log.error("Error processing renewal for user {}: {}", user.getUserEmail(), e.getMessage(), e);
        }
    }

    private String determineSubscriptionForPayment(User user) {
        String currentSubscription = user.getSubscription();

        if (TRIAL_SUBSCRIPTION.equals(currentSubscription)) {
            log.info("User {} has TRIAL subscription, upgrading to MAXIMUM for payment",
                    user.getUserEmail());
            return AFTER_TRIAL_SUBSCRIPTION;
        }

        return currentSubscription;
    }

    private SubscriptionResponse getSubscriptionData(String subscriptionName,
                                                     Map<String, SubscriptionResponse> subscriptionMap) {
        return subscriptionMap.get(subscriptionName);
    }

//    private boolean processPayment(User user, SubscriptionResponse subscriptionData) {
//        log.info("Processing payment for user {} with subscription {}",
//                user.getUserEmail(), subscriptionData.getSubscriptionName());
//
//        return paymentClient.paymentProcess(user, subscriptionData);
//    }

    private void handleSuccessfulPayment(User user, String newSubscription, SubscriptionResponse subscriptionData) {
        log.info("Payment successful for user: {}", user.getUserEmail());

        LocalDateTime newExpirationDate = calculateExpirationDate(subscriptionData);

        updateUserSubscription(user, newSubscription, newExpirationDate);

        log.info("User {} subscription updated to: {} until: {}",
                user.getUserEmail(), newSubscription, newExpirationDate);
    }

    private void handleFailedPayment(User user) {
        log.warn("Payment failed for user: {}", user.getUserEmail());

        user.setSubscription(DEFAULT_SUBSCRIPTION);
        user.setSubscriptionExpiresAt(null);

        saveUser(user);
    }

    private LocalDateTime calculateExpirationDate(SubscriptionResponse subscriptionData) {
        LocalDateTime currentTime = LocalDateTime.now();
        Integer durationInMinutes = subscriptionData.getSubscriptionDurationTime();

        return currentTime.plusMinutes(durationInMinutes);
    }

    private void updateUserSubscription(User user, String subscription, LocalDateTime expirationDate) {
        user.setSubscription(subscription);
        user.setSubscriptionExpiresAt(expirationDate);

        saveUser(user);
    }

    private void saveUser(User user) {
        try {
            userRepository.save(user);
            log.debug("User {} saved successfully", user.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to save user {}: {}", user.getUserEmail(), e.getMessage(), e);
            throw e;
        }
    }

//    private void renewUserSubscription(List<User> users) {
//        List<SubscriptionResponse> subscriptions = subscriptionClient.getAllSubscriptions();
//
//        Map<String, SubscriptionResponse> subscriptionMap = subscriptions.stream()
//                .collect(Collectors.toMap(
//                        SubscriptionResponse::getSubscriptionName,
//                        subscription -> subscription
//                ));
//
//        log.info("List of subscription from Subscription-service: " + subscriptionMap);
//
//        users.parallelStream()
//                .forEach(user -> {
//                    try {
//                        String userSubscriptionName = user.getSubscription();
//
//                        String subscriptionForPayment;
//                        if ("TRIAL".equals(userSubscriptionName)) {
//                            subscriptionForPayment = "MAXIMUM";
//                            log.info("User {} has TRIAL subscription, using MAXIMUM for payment",
//                                    user.getUserEmail());
//                        } else {
//                            subscriptionForPayment = userSubscriptionName;
//                        }
//
//                        SubscriptionResponse paymentSubscriptionData = subscriptionMap.get(subscriptionForPayment);
//
//                        if (paymentSubscriptionData == null) {
//                            log.warn("Subscription '{}' not found for user {}",
//                                    subscriptionForPayment, user.getUserEmail());
//                            return;
//                        }
//
//                        boolean paymentSuccess = paymentClient.paymentProcess(user, paymentSubscriptionData);
//
//                        if (paymentSuccess) {
//                            log.info("Payment successful for user: {}", user.getUserEmail());
//
//                            LocalDateTime currentTime = LocalDateTime.now();
//                            Integer durationInMinutes = paymentSubscriptionData.getSubscriptionDurationTime();
//                            LocalDateTime newExpirationDate = currentTime.plusMinutes(durationInMinutes);
//
//                            user.setSubscription(subscriptionForPayment);
//                            user.setSubscriptionExpiresAt(newExpirationDate);
//
//                            log.info("User {} subscription updated to: {} until: {}",
//                                    user.getUserEmail(), subscriptionForPayment, newExpirationDate);
//
//                            userRepository.save(user);
//
//                        } else {
//                            log.warn("Payment failed for user: {}", user.getUserEmail());
//                            user.setSubscription(DEFAULT_SUBSCRIPTION);
//                            userRepository.save(user);
//                        }
//
//                    } catch (Exception e) {
//                        log.error("Error processing renewal for user ID: {}: {}",
//                                user.getId(), e.getMessage(), e);
//                    }
//                });
//    }


}
