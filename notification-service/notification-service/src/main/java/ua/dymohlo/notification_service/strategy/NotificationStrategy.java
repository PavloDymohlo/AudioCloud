package ua.dymohlo.notification_service.strategy;

import ua.dymohlo.notification_service.dto.request.NotificationRequest;

public interface NotificationStrategy {
    void sendNotification(NotificationRequest request);
    String getNotificationType();
}