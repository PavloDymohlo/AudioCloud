package ua.dymohlo.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dymohlo.user_service.models.AutoSubscriptionStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userEmail;
    private String bankCardNumber;
    private String subscription;
    private AutoSubscriptionStatus autoSubscriptionStatus;
    private LocalDateTime subscriptionExpiresAt;
}
