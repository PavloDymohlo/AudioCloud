package ua.dymohlo.api_gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoadBalancerToggleService {

    private volatile boolean useFirstService = true;

    public String getNextUserService() {
        String service = useFirstService ? "user-service" : "user-service-2";
        useFirstService = !useFirstService;

        log.debug("Load balancer toggle: directing to {}", service);

        return service;
    }

    public String getCurrentService() {
        return useFirstService ? "user-service" : "user-service-2";
    }
}