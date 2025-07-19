package ua.dymohlo.notification_service.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationStrategyFactory {
    private final Map<String, NotificationStrategy> strategies;

    public NotificationStrategyFactory(List<NotificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        NotificationStrategy::getNotificationType,
                        Function.identity()
                ));
    }

    public NotificationStrategy getStrategy(String notificationType) {
        return strategies.get(notificationType);
    }
}