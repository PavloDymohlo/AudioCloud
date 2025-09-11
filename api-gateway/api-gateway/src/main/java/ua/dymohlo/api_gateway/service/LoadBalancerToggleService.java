package ua.dymohlo.api_gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.dymohlo.api_gateway.config.LoadBalancerConfig;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerToggleService {

    private final LoadBalancerConfig config;
    private volatile int currentIndex = 0;

    public String getNextUserService() {
        List<LoadBalancerConfig.ServiceConfig> availableServices = config.getServices()
                .stream()
                .filter(LoadBalancerConfig.ServiceConfig::isEnabled)
                .collect(Collectors.toList());

        if (availableServices.isEmpty()) {
            log.warn("No available services for load balancing, falling back to default");
            return "user-service";
        }

        LoadBalancerConfig.ServiceConfig selectedService = availableServices.get(
                currentIndex % availableServices.size()
        );

        currentIndex = (currentIndex + 1) % availableServices.size();

        log.debug("Load balancer selected: {}", selectedService.getName());
        return selectedService.getName();
    }

    public String getServicePort(String serviceName) {
        return config.getServices().stream()
                .filter(service -> service.getName().equals(serviceName))
                .findFirst()
                .map(LoadBalancerConfig.ServiceConfig::getPort)
                .orElse("unknown");
    }

    public String getCurrentService() {
        List<LoadBalancerConfig.ServiceConfig> availableServices = config.getServices()
                .stream()
                .filter(LoadBalancerConfig.ServiceConfig::isEnabled)
                .collect(Collectors.toList());

        if (availableServices.isEmpty()) {
            return "user-service";
        }

        return availableServices.get(currentIndex % availableServices.size()).getName();
    }
}