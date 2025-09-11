package ua.dymohlo.api_gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "load-balancer")
@Data
@Component
public class LoadBalancerConfig {
    private List<ServiceConfig> services = new ArrayList<>();
    private List<String> enabledPaths = new ArrayList<>();
    private List<String> excludedPaths = new ArrayList<>();

    @Data
    public static class ServiceConfig {
        private String name;
        private String port;
        private int weight = 1;
        private boolean enabled = true;
    }
}