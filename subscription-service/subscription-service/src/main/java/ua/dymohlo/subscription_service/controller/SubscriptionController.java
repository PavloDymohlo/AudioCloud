package ua.dymohlo.subscription_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Subscription Management", description = "Subscription plans management operations")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final SubscriptionResponseFactory subscriptionResponseFactory;

    @PostMapping()
    @Operation(
            summary = "Create new subscription plan",
            description = "Creates a new subscription plan with specified pricing and duration. Admin role required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or subscription already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public SubscriptionDataResponse createSubscription(@RequestBody CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionService.createSubscription(request);
        return subscriptionResponseFactory.createSubscriptionDataResponse(subscription);
    }

    @GetMapping("/{name}")
    @Operation(
            summary = "Get subscription plan by name",
            description = "Retrieves a specific subscription plan by its name. Publicly accessible."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription plan found and returned"),
            @ApiResponse(responseCode = "404", description = "Subscription plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public SubscriptionDataResponse getSubscriptionByName(@PathVariable String name) {
        Subscription subscription = subscriptionService.findSubscriptionByName(name);
        return subscriptionResponseFactory.createSubscriptionDataResponse(subscription);
    }

    @GetMapping()
    @Operation(
            summary = "Get all subscription plans",
            description = "Retrieves a list of all available subscription plans. Publicly accessible."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of subscription plans retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<SubscriptionDataResponse> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        return subscriptionResponseFactory.createSubscriptionDataResponseList(subscriptions);
    }

    @PutMapping()
    @Operation(
            summary = "Update subscription plan",
            description = "Updates an existing subscription plan with new information. Admin role required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription plan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Subscription plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public SubscriptionDataResponse updateSubscriptionByName(@RequestBody UpdateSubscriptionDataRequest request) {
        Subscription subscription = subscriptionService.updateSubscriptionData(request);
        return subscriptionResponseFactory.createSubscriptionDataResponse(subscription);
    }

    @DeleteMapping("/{name}")
    @Operation(
            summary = "Delete subscription plan",
            description = "Deletes a subscription plan by name. Admin role required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subscription plan deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Subscription plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> deleteSubscriptionByName(@PathVariable String name) {
        subscriptionService.deleteSubscription(name);
        return ResponseEntity.noContent().build();
    }
}
