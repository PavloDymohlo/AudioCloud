package ua.dymohlo.payment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.payment_service.client.LiqPayClient;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/liqpay")
public class LiqPayController {

    private final LiqPayClient liqPayClient;

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String data, @RequestParam String signature) {
        try {
            Map<String, Object> callbackData = liqPayClient.processCallback(data, signature);
            log.info("Received LiqPay callback: {}", callbackData);

            String status = (String) callbackData.get("status");
            String orderId = (String) callbackData.get("order_id");

            if ("success".equals(status)) {
                log.info("Payment successful for order: {}", orderId);
            } else {
                log.warn("Payment failed for order: {}", orderId);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing callback", e);
            return ResponseEntity.badRequest().body("Error");
        }
    }

    @GetMapping("/result")
    public ResponseEntity<String> handleResult(@RequestParam String data, @RequestParam String signature) {
        try {
            Map<String, Object> resultData = liqPayClient.processCallback(data, signature);
            log.info("Received LiqPay result: {}", resultData);

            return ResponseEntity.ok("Payment processed successfully");
        } catch (Exception e) {
            log.error("Error processing result", e);
            return ResponseEntity.badRequest().body("Error processing payment");
        }
    }
}