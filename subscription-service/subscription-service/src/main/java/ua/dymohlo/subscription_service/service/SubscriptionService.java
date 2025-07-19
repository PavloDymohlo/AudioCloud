package ua.dymohlo.subscription_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.subscription_service.dto.request.CreateSubscriptionRequest;
import ua.dymohlo.subscription_service.dto.request.UpdateSubscriptionDataRequest;
import ua.dymohlo.subscription_service.entity.Subscription;
import ua.dymohlo.subscription_service.exception.SubscriptionAlreadyExistsException;
import ua.dymohlo.subscription_service.exception.SubscriptionNotFoundException;
import ua.dymohlo.subscription_service.models.SubscriptionStatus;
import ua.dymohlo.subscription_service.repository.SubscriptionRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionStatus DEFAULT_SUBSCRIPTION_STATUS = SubscriptionStatus.ENABLE;

    public Subscription createSubscription(CreateSubscriptionRequest request) {
        subscriptionRepository.findBySubscriptionNameIgnoreCase(request.getSubscriptionName())
                .ifPresent(subscription -> {
                    throw new SubscriptionAlreadyExistsException("Subscription with this name already exists");
                });

        Subscription newSubscription = Subscription.builder()
                .subscriptionName(request.getSubscriptionName())
                .subscriptionPrice(request.getSubscriptionPrice())
                .subscriptionDurationTime(request.getSubscriptionDurationTime())
                .subscriptionStatus(DEFAULT_SUBSCRIPTION_STATUS)
                .build();
        log.info("subscription" + newSubscription.getSubscriptionName() + "saved");
        return subscriptionRepository.save(newSubscription);
    }

    public Subscription findSubscriptionByName(String subscriptionName) {
        return subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription with this name not found"));
    }

    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public Subscription updateSubscriptionData(UpdateSubscriptionDataRequest request) {
        return subscriptionRepository.findBySubscriptionNameIgnoreCase(request.getSubscriptionCurrentName())
                .map(existingSubscription -> {
                    existingSubscription.setSubscriptionName(request.getSubscriptionNewName());
                    existingSubscription.setSubscriptionPrice(request.getSubscriptionPrice());
                    existingSubscription.setSubscriptionDurationTime(request.getSubscriptionDurationTime());
                    existingSubscription.setSubscriptionStatus(request.getSubscriptionStatus());
                    return existingSubscription;
                })
                .map(subscriptionRepository::save)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription with current name not found"));
    }

    public void deleteSubscription(String subscriptionName) {
        subscriptionRepository.delete(
                subscriptionRepository.findBySubscriptionNameIgnoreCase(subscriptionName)
                        .orElseThrow(() -> new SubscriptionNotFoundException("Subscription with this name not found"))
        );
    }
}
