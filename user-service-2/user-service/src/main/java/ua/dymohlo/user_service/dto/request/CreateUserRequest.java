package ua.dymohlo.user_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private UUID userId;
    private String userEmail;
    private String userRole;
    private String subscriptionName;
    private String bankCardNumber;
    private String bankCardNumberCVV;
    private String bankCardNumberExpired;
}
