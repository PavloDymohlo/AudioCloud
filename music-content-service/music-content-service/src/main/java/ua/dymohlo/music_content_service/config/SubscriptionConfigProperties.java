package ua.dymohlo.music_content_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionConfigProperties {
    private Map<String, List<String>> accessRules = new HashMap<>();
}