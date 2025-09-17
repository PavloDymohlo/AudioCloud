package ua.dymohlo.user_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ua.dymohlo.user_service.dto.response.SubscriptionResponse;

import java.util.List;

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

    @Value("${subscription.scheme:http}")
    private String scheme;

    public List<SubscriptionResponse> getAllSubscriptions() {
        String url = String.format("%s://%s%s%s",
                scheme, serviceName, basePath, subscriptionsEndpoint);

        log.info("Calling subscription service at: {}", url);

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<SubscriptionResponse>>() {
                    })
                    .block();

        } catch (Exception e) {
            log.error("Error calling subscription service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get subscriptions", e);
        }
    }
}