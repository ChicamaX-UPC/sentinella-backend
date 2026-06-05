package com.chicamax.sentinella.payments.infrastructure.webhook;

import com.chicamax.sentinella.payments.domain.services.PaymentCommandService;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Stub de integración Stripe; confirma pagos vía endpoint demo. */
@RestController
@RequestMapping("/v1/payments/webhooks/stripe")
public class StripeWebhookAdapter {

    private final PaymentCommandService paymentCommandService;

    public StripeWebhookAdapter(PaymentCommandService paymentCommandService) {
        this.paymentCommandService = paymentCommandService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> receive(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("status", "ignored", "mode", "stub"));
    }

    @PostMapping("/confirm/{paymentId}")
    public ResponseEntity<Map<String, Object>> confirmDemo(@PathVariable UUID paymentId) {
        var payment = paymentCommandService.confirmPayment(paymentId);
        return ResponseEntity.ok(Map.of(
                "paymentId", payment.getId(),
                "status", payment.getStatus().name()
        ));
    }
}
