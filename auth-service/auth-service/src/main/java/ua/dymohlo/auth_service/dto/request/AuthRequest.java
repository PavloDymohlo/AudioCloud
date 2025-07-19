package ua.dymohlo.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String userEmail;
    @NotBlank(message = "Password is requires")
    private String password;
    //bottom for test
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    private String bankCardNumber;
}
