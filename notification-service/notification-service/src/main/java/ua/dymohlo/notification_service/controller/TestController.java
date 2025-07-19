package ua.dymohlo.notification_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.notification_service.dto.request.NotificationRequest;
import ua.dymohlo.notification_service.strategy.EmailNotificationStrategy;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {

    private final EmailNotificationStrategy emailStrategy;

    @PostMapping("/email")
    public ResponseEntity<String> testEmail(@RequestBody NotificationRequest request) {
        try {
            log.info("Testing email notification to: {}", request.getRecipient());

            emailStrategy.sendNotification(request);

            return ResponseEntity.ok("Email sent successfully to: " + request.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send test email", e);
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Notification service is running!");
    }
}