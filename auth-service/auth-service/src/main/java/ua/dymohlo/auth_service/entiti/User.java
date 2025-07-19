package ua.dymohlo.auth_service.entiti;

import jakarta.persistence.*;
import lombok.*;
import ua.dymohlo.auth_service.models.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auth_credentials", schema = "auth_service")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String userEmail;
    private String userPassword;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userLastLogin;
}
