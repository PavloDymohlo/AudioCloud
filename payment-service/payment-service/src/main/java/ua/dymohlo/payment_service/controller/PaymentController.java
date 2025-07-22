package ua.dymohlo.payment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.payment_service.dto.request.PaymentRequest;
import ua.dymohlo.payment_service.dto.response.PaymentResponse;
import ua.dymohlo.payment_service.service.PaymentService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{paymentType}")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable String paymentType,
            @RequestBody PaymentRequest request) {

        log.info("Processing payment request: type={}, generally={}", paymentType, request);

        PaymentResponse response = paymentService.processPayment(request, paymentType);

        return ResponseEntity.ok(response);
    }
}