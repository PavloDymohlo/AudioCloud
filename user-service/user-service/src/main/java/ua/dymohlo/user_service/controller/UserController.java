package ua.dymohlo.user_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.user_service.dto.request.CreateUserRequest;
import ua.dymohlo.user_service.dto.request.UserProfileDataRequest;
import ua.dymohlo.user_service.dto.response.UserResponse;
import ua.dymohlo.user_service.entity.User;
import ua.dymohlo.user_service.models.AutoSubscriptionStatus;
import ua.dymohlo.user_service.security.util.JwtUtil;
import ua.dymohlo.user_service.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping()
    public UserResponse createUser(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody CreateUserRequest request) {
        CreateUserRequest createRequest = CreateUserRequest.builder()
                .userId(jwtUtil.getUserIdFromToken(authHeader))
                .userEmail(jwtUtil.getUsernameFromToken(authHeader))
                .userRole(jwtUtil.getRoleFromToken(authHeader))
                .subscriptionName(request.getSubscriptionName())
                .bankCardNumber(request.getBankCardNumber())
                .bankCardNumberCVV(request.getBankCardNumberCVV())
                .bankCardNumberExpired(request.getBankCardNumberExpired()).build();

        User user = userService.createNewUser(createRequest);

        return UserResponse.builder()
                .userEmail(user.getUserEmail())
                .bankCardNumber(user.getBankCardNumber())
                .subscription(user.getSubscription())
                .autoSubscriptionStatus(user.getAutoSubscription())
                .subscriptionExpiresAt(user.getSubscriptionExpiresAt()).build();
    }

    @GetMapping("/profile")
    public UserResponse getUserProfile(@RequestHeader("Authorization") String authHeader) {
        UserProfileDataRequest request = UserProfileDataRequest.builder()
                .userId(jwtUtil.getUserIdFromToken(authHeader))
                .userEmail(jwtUtil.getUsernameFromToken(authHeader))
                .userRole(jwtUtil.getRoleFromToken(authHeader)).build();

        User user = userService.getUserProfile(request);

        return UserResponse.builder()
                .userEmail(user.getUserEmail())
                .bankCardNumber(user.getBankCardNumber())
                .subscription(user.getSubscription())
                .autoSubscriptionStatus(user.getAutoSubscription())
                .subscriptionExpiresAt(user.getSubscriptionExpiresAt()).build();
    }

    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @GetMapping("/emails/{email}")
    public User findUserByEmail(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }

    @GetMapping("/subscriptions/{subscription}")
    public Page<User> findUserBySubscription(
            @PathVariable String subscription,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userService.findUserBySubscription(subscription, pageable);
    }

    @GetMapping()
    public Page<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return userService.getAllUsers(pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @PutMapping("/emails/{email}/auto-subscription")
    public User updateUserAutoSubscriptionStatus(@PathVariable String email,
                                                 @RequestBody AutoSubscriptionStatus status) {
        return userService.updateUserAutoSubscriptionStatus(email, status);
    }

    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    @PutMapping("/emails/{email}/subscription/{subscription}")
    public User updateUserSubscription(@PathVariable String email,
                                       @PathVariable String subscription) {
        return userService.updateUserSubscription(email, subscription);
    }

    @DeleteMapping("/emails/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}
