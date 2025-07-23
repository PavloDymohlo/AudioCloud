package ua.dymohlo.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dymohlo.auth_service.entiti.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredUserInfoResponse {
    private User user;
    private String subscriptionName;
}
