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
import ua.dymohlo.api_gateway.config.LoadBalancerConfig;
import ua.dymohlo.api_gateway.service.LoadBalancerToggleService;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceLoadBalancerFilter implements GlobalFilter, Ordered {

    private final LoadBalancerToggleService toggleService;
    private final LoadBalancerConfig config;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.info("Processing request: {} {}", request.getMethod(), path);

        if (isTargetRequest(path)) {
            String targetService = toggleService.getNextUserService();
            String servicePort = toggleService.getServicePort(targetService);

            log.info("Load balancing request: {} -> {} (port: {})", path, targetService, servicePort);

            URI newUri = URI.create("lb://" + targetService);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Target-Service", targetService)
                    .header("X-Forwarded-Service", targetService)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            modifiedExchange.getAttributes().put(
                    "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR",
                    newUri
            );

            String serviceInfo = targetService + " (port: " + servicePort + ")";
            exchange.getResponse().getHeaders().add("X-Routed-To-Service", serviceInfo);
            log.info("Added response header: X-Routed-To-Service = {}", serviceInfo);

            return chain.filter(modifiedExchange);
        }

        return chain.filter(exchange);
    }

    private boolean isTargetRequest(String path) {
        boolean isEnabledPath = config.getEnabledPaths().stream()
                .anyMatch(path::startsWith);

        boolean isExcludedPath = config.getExcludedPaths().stream()
                .anyMatch(path::contains);

        log.info("Checking path: {}, isEnabled: {}, isExcluded: {}",
                path, isEnabledPath, isExcludedPath);

        return isEnabledPath && !isExcludedPath;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

