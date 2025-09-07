package ua.dymohlo.api_gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ua.dymohlo.api_gateway.service.LoadBalancerToggleService;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceLoadBalancerFilter implements GlobalFilter, Ordered {

    private final LoadBalancerToggleService toggleService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Перевіряємо чи це запит до user-service
        if (isUserServiceRequest(path)) {
            String targetService = toggleService.getNextUserService();

            log.info("Load balancing user service request: {} -> {}", path, targetService);

            // Модифікуємо URI для перенаправлення на обраний сервіс
            URI newUri = URI.create("lb://" + targetService);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Target-Service", targetService)
                    .header("X-Forwarded-Service", targetService)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            // Встановлюємо новий URI в атрибути exchange
            modifiedExchange.getAttributes().put("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR", newUri);

            return chain.filter(modifiedExchange);
        }

        return chain.filter(exchange);
    }

    private boolean isUserServiceRequest(String path) {
        return path.startsWith("/api/v1/users") && !path.contains("/saga");
    }

    @Override
    public int getOrder() {
        return -1; // Виконується перед іншими фільтрами
    }
}