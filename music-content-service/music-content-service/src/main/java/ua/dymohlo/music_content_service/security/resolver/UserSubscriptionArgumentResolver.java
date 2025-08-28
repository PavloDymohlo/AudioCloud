package ua.dymohlo.music_content_service.security.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ua.dymohlo.music_content_service.config.SubscriptionConfigProperties;
import ua.dymohlo.music_content_service.dto.event.UserSubscriptionEvent;
import ua.dymohlo.music_content_service.dto.security.UserAccessInfo;
import ua.dymohlo.music_content_service.security.annotation.UserSubscription;
import ua.dymohlo.music_content_service.security.util.JwtUtil;
import ua.dymohlo.music_content_service.service.UserSubscriptionCacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserSubscriptionArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;
    private final UserSubscriptionCacheService cacheService;
    private final SubscriptionConfigProperties subscriptionConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserSubscription.class) &&
                parameter.getParameterType().equals(UserAccessInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found");
            return createDefaultUserAccess();
        }

        String token = authHeader.substring(7);

        try {
            UUID userId = jwtUtil.getUserIdFromToken(token);
            log.debug("Extracted userId from token: {}", userId);

            String cachedJson = cacheService.getUserSubscription(userId);
            if (cachedJson == null) {
                log.warn("No subscription found in cache for userId: {}", userId);
                return createDefaultUserAccess(userId);
            }

            UserSubscriptionEvent event = objectMapper.readValue(cachedJson, UserSubscriptionEvent.class);
            String userSubscription = event.getSubscription();
            log.debug("User subscription from cache: {}", userSubscription);

            List<String> allowedTypes = subscriptionConfig.getAccessRules()
                    .getOrDefault(userSubscription, new ArrayList<>());

            log.debug("Allowed subscription types for {}: {}", userSubscription, allowedTypes);

            return UserAccessInfo.builder()
                    .userId(userId)
                    .userSubscription(userSubscription)
                    .allowedSubscriptionTypes(allowedTypes)
                    .build();

        } catch (Exception e) {
            log.error("Error resolving user subscription", e);
            return createDefaultUserAccess();
        }
    }

    private UserAccessInfo createDefaultUserAccess() {
        return createDefaultUserAccess(null);
    }

    private UserAccessInfo createDefaultUserAccess(UUID userId) {
        return UserAccessInfo.builder()
                .userId(userId)
                .userSubscription("NONE")
                .allowedSubscriptionTypes(new ArrayList<>())
                .build();
    }
}