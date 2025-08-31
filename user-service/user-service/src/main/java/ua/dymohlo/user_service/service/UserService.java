package ua.dymohlo.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.dymohlo.user_service.client.PaymentClient;
import ua.dymohlo.user_service.client.SubscriptionClient;
import ua.dymohlo.user_service.dto.request.CreateUserRequest;
import ua.dymohlo.user_service.dto.request.CreateUserSagaRequest;
import ua.dymohlo.user_service.dto.request.UserProfileDataRequest;
import ua.dymohlo.user_service.dto.response.SubscriptionResponse;
import ua.dymohlo.user_service.entity.User;
import ua.dymohlo.user_service.exception.PaymentFailedException;
import ua.dymohlo.user_service.exception.UserNotFoundException;
import ua.dymohlo.user_service.models.AutoSubscriptionStatus;
import ua.dymohlo.user_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserSubscriptionPublisher subscriptionPublisher;
    private final SubscriptionClient subscriptionClient;
    private final PaymentClient paymentClient;
    private final UserDeletedPublisher userDeletedPublisher;
    private final AutoSubscriptionStatus DEFAULT_AUTO_SUBSCRIPTION_STATUS = AutoSubscriptionStatus.YES;

    public User getUserProfile(UserProfileDataRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Data for this user don't found!"));

        subscriptionPublisher.publishUserSubscriptionEvent(user.getId(), user.getSubscription());
        log.info("User {} accessed profile - sent subscription data to Kafka: {}",
                user.getUserEmail(), user.getSubscription());

        return user;
    }

    public User createNewUser(CreateUserRequest request) {
        User newUser = User.builder()
                .id(request.getUserId())
                .autoSubscription(DEFAULT_AUTO_SUBSCRIPTION_STATUS)
                .subscription(request.getSubscriptionName())
                .userEmail(request.getUserEmail())
                .userRole(request.getUserRole())
                .bankCardNumber(request.getBankCardNumber())
                .bankCardCvv(request.getBankCardNumberCVV())
                .bankCardExpired(request.getBankCardNumberExpired())
                .subscriptionExpiresAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        subscriptionPublisher.publishUserSubscriptionEvent(
                savedUser.getId(),
                savedUser.getSubscription()
        );

        return savedUser;
    }

    public User findUserByEmail(String userEmail) {
        return userRepository.findUserByUserEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
    }

    public Page<User> findUserBySubscription(String subscriptionName, Pageable pageable) {
        return userRepository.findUserBySubscription(subscriptionName, pageable)
                .orElse(Page.empty());
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updateUserAutoSubscriptionStatus(String email, AutoSubscriptionStatus status) {
        return userRepository.findUserByUserEmail(email)
                .map(user -> {
                    user.setAutoSubscription(status);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
    }


    public User updateUserSubscription(String email, String subscription) {
        return userRepository.findUserByUserEmail(email)
                .map(user -> {
                    List<SubscriptionResponse> subscriptions = subscriptionClient.getAllSubscriptions();
                    SubscriptionResponse newSubscription = subscriptions.stream()
                            .filter(s -> s.getSubscriptionName().equals(subscription))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Subscription not found"));

                    boolean paymentSuccess = paymentClient.paymentProcess(user, newSubscription);

                    if (!paymentSuccess) {
                        throw new PaymentFailedException("Payment failed");
                    }

                    user.setSubscription(subscription);
                    user.setSubscriptionExpiresAt(
                            LocalDateTime.now().plusMinutes(newSubscription.getSubscriptionDurationTime())
                    );
                    User savedUser = userRepository.save(user);

                    subscriptionPublisher.publishUserSubscriptionEvent(
                            savedUser.getId(),
                            savedUser.getSubscription()
                    );

                    return savedUser;
                })
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
    }

    public void deleteUser(String email) {
        User user = userRepository.findUserByUserEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
        userRepository.delete(user);

        userDeletedPublisher.publishUserDeletedEvent(email);
    }
    public User createUserFromSaga(CreateUserSagaRequest request) {
        User newUser = User.builder()
                .id(request.getUserId())
                .autoSubscription(DEFAULT_AUTO_SUBSCRIPTION_STATUS)
                .subscription(request.getSubscriptionName())
                .userEmail(request.getUserEmail())
                .userRole(request.getUserRole())
                .bankCardNumber(request.getBankCardNumber())
                .bankCardCvv(request.getBankCardNumberCVV())
                .bankCardExpired(request.getBankCardNumberExpired())
                .subscriptionExpiresAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        subscriptionPublisher.publishUserSubscriptionEvent(
                savedUser.getId(),
                savedUser.getSubscription()
        );

        log.info("User created from SAGA: userId={}, email={}", savedUser.getId(), savedUser.getUserEmail());
        return savedUser;
    }

    public void deleteUserFromSaga(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for SAGA compensation: " + userId));

        userRepository.delete(user);
        log.info("User deleted from SAGA compensation: userId={}, email={}", userId, user.getUserEmail());
    }
}