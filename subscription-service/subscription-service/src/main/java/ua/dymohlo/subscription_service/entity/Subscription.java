package ua.dymohlo.subscription_service.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.dymohlo.subscription_service.models.SubscriptionStatus;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscription_plans", schema = "subscription_service")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subscriptionName;
    private BigDecimal subscriptionPrice;
    private Integer subscriptionDurationTime;
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;
}
