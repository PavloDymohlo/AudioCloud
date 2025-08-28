package ua.dymohlo.music_content_service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import ua.dymohlo.music_content_service.config.SubscriptionConfigProperties;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class MusicContentServiceApplication {
    private final SubscriptionConfigProperties subscriptionConfig;
    public static void main(String[] args) {
        SpringApplication.run(MusicContentServiceApplication.class, args);
    }
    @PostConstruct
    public void logConfig() {
        log.info("=== CONFIG SERVICE DATA ===");
        log.info("Subscription rules: {}", subscriptionConfig.getAccessRules());
    }
    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh() {
        log.info("=== REFRESHED CONFIG ===");
        log.info("New subscription rules: {}", subscriptionConfig.getAccessRules());
    }
}
