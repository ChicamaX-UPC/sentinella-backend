package com.chicamax.sentinella.payments.infrastructure.stripe;

import com.chicamax.sentinella.payments.domain.model.aggregates.BillingCustomer;
import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.BillingCustomerRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StripeCheckoutService {

    private final StripeProperties stripeProperties;
    private final BillingCustomerRepository billingCustomerRepository;

    public StripeCheckoutService(StripeProperties stripeProperties, BillingCustomerRepository billingCustomerRepository) {
        this.stripeProperties = stripeProperties;
        this.billingCustomerRepository = billingCustomerRepository;
    }

    public Session createSubscriptionCheckout(Payment payment, Plan plan, String customerEmail) {
        if (!stripeProperties.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe no configurado");
        }

        String recurringPriceId = resolveRecurringPriceId(plan);
        String setupPriceId = resolveSetupPriceId(plan);
        BillingCustomer customer = resolveCustomer(payment.getUserId(), customerEmail);

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        lineItems.add(SessionCreateParams.LineItem.builder()
                .setPrice(recurringPriceId)
                .setQuantity(1L)
                .build());
        if (setupPriceId != null && !setupPriceId.isBlank()) {
            lineItems.add(SessionCreateParams.LineItem.builder()
                    .setPrice(setupPriceId)
                    .setQuantity(1L)
                    .build());
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("paymentId", payment.getId().toString());
        metadata.put("userId", payment.getUserId().toString());
        metadata.put("planId", payment.getPlanId().toString());

        try {
            Session session = Session.create(SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customer.getStripeCustomerId())
                    .setClientReferenceId(payment.getId().toString())
                    .setSuccessUrl(stripeProperties.successUrl())
                    .setCancelUrl(stripeProperties.cancelUrl())
                    .setPaymentMethodCollection(SessionCreateParams.PaymentMethodCollection.ALWAYS)
                    .putAllMetadata(metadata)
                    .addAllLineItem(lineItems)
                    .build());
            return session;
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo crear la sesión de Stripe", ex);
        }
    }

    public String createPortalSession(UUID userId) {
        if (!stripeProperties.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe no configurado");
        }
        BillingCustomer customer = billingCustomerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente de facturación no encontrado"));

        try {
            com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(customer.getStripeCustomerId())
                            .setReturnUrl(stripeProperties.portalReturnUrl())
                            .build()
            );
            return session.getUrl();
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo abrir el portal de Stripe", ex);
        }
    }

    private BillingCustomer resolveCustomer(UUID userId, String email) {
        return billingCustomerRepository.findByUserId(userId).orElseGet(() -> {
            try {
                Customer stripeCustomer = Customer.create(CustomerCreateParams.builder()
                        .setEmail(email)
                        .putMetadata("userId", userId.toString())
                        .build());
                return billingCustomerRepository.save(BillingCustomer.create(userId, stripeCustomer.getId(), email));
            } catch (StripeException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo crear el cliente en Stripe", ex);
            }
        });
    }

    private String resolveRecurringPriceId(Plan plan) {
        if (plan.getStripePriceId() != null && !plan.getStripePriceId().isBlank()) {
            return plan.getStripePriceId();
        }
        StripeProperties.StripePlanPriceIds configured = stripeProperties.resolvePlanPrices(plan.getCode());
        if (configured != null && configured.recurringPriceId() != null && !configured.recurringPriceId().isBlank()) {
            return configured.recurringPriceId();
        }
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Precio recurrente de Stripe no configurado para el plan " + plan.getCode()
        );
    }

    private String resolveSetupPriceId(Plan plan) {
        if (plan.getStripeSetupPriceId() != null && !plan.getStripeSetupPriceId().isBlank()) {
            return plan.getStripeSetupPriceId();
        }
        StripeProperties.StripePlanPriceIds configured = stripeProperties.resolvePlanPrices(plan.getCode());
        if (configured != null && configured.setupPriceId() != null && !configured.setupPriceId().isBlank()) {
            return configured.setupPriceId();
        }
        return null;
    }
}
