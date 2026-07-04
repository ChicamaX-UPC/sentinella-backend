package com.chicamax.sentinella.payments.interfaces.rest;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.services.PaymentCommandService;
import com.chicamax.sentinella.payments.infrastructure.stripe.StripeCheckoutConfirmationService;
import com.chicamax.sentinella.payments.interfaces.rest.resources.CheckoutConfirmResource;
import com.chicamax.sentinella.payments.interfaces.rest.resources.CheckoutResource;
import com.chicamax.sentinella.payments.interfaces.rest.resources.PaymentResource;
import com.chicamax.sentinella.payments.interfaces.rest.resources.PlanResource;
import com.chicamax.sentinella.payments.interfaces.rest.resources.PortalResource;
import com.chicamax.sentinella.payments.interfaces.rest.transform.PaymentResourceAssembler;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentResourceAssembler paymentResourceAssembler;
    private final StripeCheckoutConfirmationService stripeCheckoutConfirmationService;

    public PaymentController(
            PaymentCommandService paymentCommandService,
            PaymentResourceAssembler paymentResourceAssembler,
            StripeCheckoutConfirmationService stripeCheckoutConfirmationService
    ) {
        this.paymentCommandService = paymentCommandService;
        this.paymentResourceAssembler = paymentResourceAssembler;
        this.stripeCheckoutConfirmationService = stripeCheckoutConfirmationService;
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResource>> listPlans() {
        var plans = paymentCommandService.listPlans().stream()
                .map(paymentResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(plans);
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentResource> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutResource resource
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = paymentCommandService.createCheckout(paymentResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(paymentResourceAssembler.toResource(result));
    }

    @PostMapping("/portal")
    public ResponseEntity<PortalResource> portal(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String portalUrl = paymentCommandService.createPortalSession(userId);
        return ResponseEntity.ok(new PortalResource(portalUrl));
    }

    /** Confirma pago al volver de Stripe Checkout (fallback si el webhook no llegó). */
    @PostMapping("/checkout/confirm")
    public ResponseEntity<PaymentResource> confirmCheckout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutConfirmResource resource
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Payment payment = stripeCheckoutConfirmationService.confirmSession(userId, resource.sessionId());
        return ResponseEntity.ok(paymentResourceAssembler.toResource(payment, null));
    }
}
