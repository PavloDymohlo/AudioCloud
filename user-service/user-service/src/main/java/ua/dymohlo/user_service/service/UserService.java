package ua.dymohlo.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.dymohlo.user_service.dto.request.UserProfileDataRequest;
import ua.dymohlo.user_service.entity.User;
import ua.dymohlo.user_service.exception.UserNotFoundException;
import ua.dymohlo.user_service.models.AutoSubscriptionStatus;
import ua.dymohlo.user_service.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AutoSubscriptionStatus DEFAULT_AUTO_SUBSCRIPTION_STATUS = AutoSubscriptionStatus.YES;

    public User checkUserUUID(UserProfileDataRequest request) {
        return userRepository.findById(request.getUserId())
                .orElseGet(() -> createNewUser(request));
    }

    private User createNewUser(UserProfileDataRequest request) {
        User newUser = User.builder()
                .id(request.getUserId())
                .autoSubscription(DEFAULT_AUTO_SUBSCRIPTION_STATUS)
                .subscription("Mock")
                .userEmail(request.getUserEmail())
                .userRole(request.getUserRole())
                .subscriptionExpiresAt(LocalDateTime.now()).build();
        return userRepository.save(newUser);
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
                    user.setSubscription(subscription);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
    }

    public void deleteUser(String email) {
        User user = userRepository.findUserByUserEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));
        userRepository.delete(user);
    }

}
