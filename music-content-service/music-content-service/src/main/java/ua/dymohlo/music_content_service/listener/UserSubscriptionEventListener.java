package ua.dymohlo.music_content_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserSubscriptionEventListener {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cache.user-subscription.key-prefix}")
    private String keyPrefix;

    @Value("${cache.user-subscription.ttl-hours}")
    private int ttlHours;
    private static final Pattern USER_ID_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*\"([^\"]+)\"");

    @KafkaListener(topics = "${kafka.topics.user-subscriptions}")
    public void handleUserSubscriptionEvent(String jsonMessage) {
        try {
            String userId = extractUserId(jsonMessage);

            if (userId != null) {
                String redisKey = keyPrefix + userId;
                Duration ttl = Duration.ofHours(ttlHours);
                redisTemplate.opsForValue().set(redisKey, jsonMessage, ttl);

                log.info("Successfully cached raw JSON: key={}, userId={}", redisKey, userId);
            } else {
                log.warn("Could not extract userId from JSON: {}", jsonMessage);
            }

        } catch (Exception e) {
            log.error("Failed to process raw JSON message: {}", jsonMessage, e);
        }
    }

    private String extractUserId(String jsonMessage) {
        if (jsonMessage == null || jsonMessage.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = USER_ID_PATTERN.matcher(jsonMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}