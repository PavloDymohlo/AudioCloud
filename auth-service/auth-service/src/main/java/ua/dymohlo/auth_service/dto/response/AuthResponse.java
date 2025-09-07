package ua.dymohlo.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String subscriptionName;
    private String bankCardNumber;
    private String bankCardNumberCVV;
    private String bankCardNumberExpired;
}
