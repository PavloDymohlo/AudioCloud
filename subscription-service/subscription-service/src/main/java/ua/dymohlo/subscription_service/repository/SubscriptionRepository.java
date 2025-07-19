package ua.dymohlo.subscription_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.subscription_service.entity.Subscription;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriptionNameIgnoreCase(String subscriptionName);
}
