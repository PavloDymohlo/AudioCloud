package ua.dymohlo.music_content_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSubscriptionCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cache.user-subscription.key-prefix}")
    private String keyPrefix;

    @Value("${cache.user-subscription.ttl-hours}")
    private int ttlHours;

    public void saveUserSubscription(UUID userId, String subscription) {
        String key = keyPrefix + userId.toString();
        Duration ttl = Duration.ofHours(ttlHours);

        try {
            redisTemplate.opsForValue().set(key, subscription, ttl);
            log.info("Saved user subscription: userId={}, subscription={}", userId, subscription);
        } catch (Exception e) {
            log.error("Failed to save user subscription to Redis: userId={}", userId, e);
        }
    }

    public String getUserSubscription(UUID userId) {
        String key = keyPrefix + userId.toString();
        log.info("key for userID {} is {}", userId, key);
        try {
            String subscription = redisTemplate.opsForValue().get(key);
            log.info("Subscription for key {} is {}", key, subscription);
            return subscription;
        } catch (Exception e) {
            log.error("Failed to get user subscription from Redis: userId={}", userId, e);
            return null;
        }
    }
}