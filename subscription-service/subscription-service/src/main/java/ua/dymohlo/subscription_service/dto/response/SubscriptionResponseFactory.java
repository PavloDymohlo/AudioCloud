package ua.dymohlo.subscription_service.dto.response;

import org.springframework.stereotype.Component;
import ua.dymohlo.subscription_service.entity.Subscription;

import java.util.List;

@Component
public class SubscriptionResponseFactory {
    public SubscriptionDataResponse createSubscriptionDataResponse(Subscription subscription) {
        return SubscriptionDataResponse.builder()
                .subscriptionName(subscription.getSubscriptionName())
                .subscriptionPrice(subscription.getSubscriptionPrice())
                .subscriptionDurationTime(subscription.getSubscriptionDurationTime()).build();
    }

    public List<SubscriptionDataResponse> createSubscriptionDataResponseList(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(subscriptionPlans -> new SubscriptionDataResponse(
                                subscriptionPlans.getSubscriptionName(),
                                subscriptionPlans.getSubscriptionPrice(),
                                subscriptionPlans.getSubscriptionDurationTime()
                        )
                )
                .toList();
    }
}
