package ua.dymohlo.subscription_service.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.subscription_service.dto.request.CreateSubscriptionRequest;
import ua.dymohlo.subscription_service.dto.request.UpdateSubscriptionDataRequest;
import ua.dymohlo.subscription_service.dto.response.SubscriptionDataResponse;
import ua.dymohlo.subscription_service.dto.response.SubscriptionResponseFactory;
import ua.dymohlo.subscription_service.entity.Subscription;
import ua.dymohlo.subscription_service.service.SubscriptionService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final SubscriptionResponseFactory subscriptionResponseFactory;

    @PostMapping()
    public SubscriptionDataResponse createSubscription(@RequestBody CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionService.createSubscription(request);
        return subscriptionResponseFactory.createSubscriptionDataResponse(subscription);
    }

    @GetMapping("/{name}")
    public SubscriptionDataResponse getSubscriptionByName(@PathVariable String name) {
        Subscription subscription = subscriptionService.findSubscriptionByName(name);
        return subscriptionResponseFactory.createSubscriptionDataResponse(subscription);
    }

    @GetMapping()
    public List<SubscriptionDataResponse> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        return subscriptionResponseFactory.createSubscriptionDataResponseList(subscriptions);
    }

    @PutMapping()
    public SubscriptionDataResponse updateSubscriptionByName(@RequestBody UpdateSubscriptionDataRequest request) {
        Subscription subscription = subscriptionService.updateSubscriptionData(request);
        return subscriptionResponseFactory.createSubscriptionDataResponse(subscription);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteSubscriptionByName(@PathVariable String name) {
        subscriptionService.deleteSubscription(name);
        return ResponseEntity.noContent().build();
    }
}
