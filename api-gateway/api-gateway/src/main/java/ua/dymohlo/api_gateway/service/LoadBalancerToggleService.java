package ua.dymohlo.api_gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ua.dymohlo.api_gateway.config.LoadBalancerConfig;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerToggleService {

    private final LoadBalancerConfig config;

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public String getNextUserService() {
        List<LoadBalancerConfig.ServiceConfig> availableServices = getAvailableServices();

        if (availableServices.isEmpty()) {
            log.warn("No available services for load balancing, falling back to default");
            return "user-service";
        }

        int index = currentIndex.getAndIncrement() % availableServices.size();
        LoadBalancerConfig.ServiceConfig selectedService = availableServices.get(index);

        log.info("Load balancer selected: {} (index: {}, total services: {})",
                selectedService.getName(), index, availableServices.size());

        logAvailableServices(availableServices, selectedService);

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
        List<LoadBalancerConfig.ServiceConfig> availableServices = getAvailableServices();

        if (availableServices.isEmpty()) {
            return "user-service";
        }

        int index = currentIndex.get() % availableServices.size();
        return availableServices.get(index).getName();
    }

    private List<LoadBalancerConfig.ServiceConfig> getAvailableServices() {
        List<LoadBalancerConfig.ServiceConfig> availableServices = config.getServices()
                .stream()
                .filter(LoadBalancerConfig.ServiceConfig::isEnabled)
                .collect(Collectors.toList());

        log.debug("Available services count: {}", availableServices.size());
        return availableServices;
    }

    private void logAvailableServices(List<LoadBalancerConfig.ServiceConfig> availableServices,
                                      LoadBalancerConfig.ServiceConfig selectedService) {
        log.info("Available services:");
        for (int i = 0; i < availableServices.size(); i++) {
            LoadBalancerConfig.ServiceConfig service = availableServices.get(i);
            String marker = service.equals(selectedService) ? " <- SELECTED" : "";
            log.info("  [{}] {} (port: {}, enabled: {}){}",
                    i, service.getName(), service.getPort(), service.isEnabled(), marker);
        }
    }

    public void resetCounter() {
        currentIndex.set(0);
        log.info("Load balancer counter reset to 0");
    }

    public void logStatistics() {
        List<LoadBalancerConfig.ServiceConfig> availableServices = getAvailableServices();
        log.info("Load Balancer Statistics:");
        log.info("  Total requests processed: {}", currentIndex.get());
        log.info("  Available services: {}", availableServices.size());
        log.info("  Current index: {}", currentIndex.get());

        if (!availableServices.isEmpty()) {
            int nextIndex = currentIndex.get() % availableServices.size();
            log.info("  Next service will be: {}", availableServices.get(nextIndex).getName());
        }
    }
}