package ua.dymohlo.api_gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 This clas I maybe will use for testing with real users*/

//@Component
@Slf4j
public class GlobalServiceOrchestrationFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${user-service.name:user-service}")
    private String userServiceName;

    @Value("${user-service.api.base-path:/api/v1/users}")
    private String userServiceBasePath;

    public GlobalServiceOrchestrationFilter(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        this.webClient = WebClient.builder()
                .filter(lbFunction)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("=== GlobalServiceOrchestrationFilter for path: {} ===", path);

        if (!isRegistrationPath(path)) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponse decoratedResponse = new org.springframework.http.server.reactive.ServerHttpResponseDecorator(
                originalResponse) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                log.info("=== writeWith called for registration, status: {} ===", getStatusCode());

                if (body instanceof Flux && getStatusCode() == HttpStatus.OK) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    return super.writeWith(fluxBody.buffer().flatMap(dataBuffers -> {
                        DataBuffer joinedBuffer = bufferFactory.join(dataBuffers);
                        byte[] content = new byte[joinedBuffer.readableByteCount()];
                        joinedBuffer.read(content);
                        DataBufferUtils.release(joinedBuffer);

                        String responseBody = new String(content, StandardCharsets.UTF_8);
                        log.info("Auth Service Response: {}", responseBody);

                        try {
                            JsonNode authResponse = objectMapper.readTree(responseBody);
                            String token = authResponse.get("token").asText();

                            String userServiceRequest = createUserServiceRequest(authResponse);

                            return callUserService(token, userServiceRequest)
                                    .map(userResponse -> {
                                        log.info("User Service Response: {}", userResponse);
                                        return bufferFactory.wrap(responseBody.getBytes(StandardCharsets.UTF_8));
                                    })
                                    .onErrorReturn(bufferFactory.wrap(responseBody.getBytes(StandardCharsets.UTF_8)));

                        } catch (Exception e) {
                            log.error("Error processing registration response", e);
                            return Mono.just(bufferFactory.wrap(content));
                        }
                    }));
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private boolean isRegistrationPath(String path) {
        return path.contains("/api/v1/auth/register");
    }

    private String createUserServiceRequest(JsonNode authResponse) throws Exception {
        ObjectNode userRequest = authResponse.deepCopy();
        userRequest.remove("token");

        String result = objectMapper.writeValueAsString(userRequest);
        log.info("User Service Request: {}", result);
        return result;
    }

    private Mono<String> callUserService(String token, String requestBody) {
        String userServiceUrl = String.format("lb://%s%s", userServiceName, userServiceBasePath);

        return webClient.post()
                .uri(userServiceUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Successfully called User Service"))
                .doOnError(error -> log.error("Error calling User Service: {}", error.getMessage()));
    }

    @Override
    public int getOrder() {
        return -2;
    }
}