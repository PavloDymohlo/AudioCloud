package ua.dymohlo.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.dymohlo.user_service.models.AutoSubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema = "user_service")
public class User {
    @Id
    @Column(name = "uuid_id")
    private UUID id;
    @Enumerated(EnumType.STRING)
    private AutoSubscriptionStatus autoSubscription;
    private String subscription;
    private LocalDateTime subscriptionExpiresAt;
    private String userEmail;
    @Column(name = "user_role")
    private String userRole;
    private String bankCardNumber;
    private String bankCardCvv;
    private String bankCardExpired;
}
