package ua.dymohlo.auth_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionClient {

    private final WebClient webClient;

    @Value("${subscription.service-name}")
    private String serviceName;

    @Value("${subscription.api.base-path}")
    private String basePath;

    @Value("${subscription.api.subscriptions-endpoint}")
    private String subscriptionsEndpoint;

    @Value("${subscription.default-plan}")
    private String defaultPlan;
    @Value("${subscription.scheme:http}")
    private String scheme;

    public String getDefaultSubscription() {
        String url = String.format("%s://%s%s%s/%s",
                scheme, serviceName, basePath, subscriptionsEndpoint, defaultPlan);

        log.info("Calling subscription service at: {}", url);

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error calling subscription service: {}", e.getMessage());
            throw new RuntimeException("Failed to get default subscription", e);
        }
    }
}