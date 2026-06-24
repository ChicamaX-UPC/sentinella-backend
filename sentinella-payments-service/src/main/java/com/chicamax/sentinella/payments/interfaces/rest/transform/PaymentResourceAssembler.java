package com.chicamax.sentinella.payments.interfaces.rest.transform;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import com.chicamax.sentinella.payments.domain.model.commands.CreateCheckoutCommand;
import com.chicamax.sentinella.payments.domain.model.valueobjects.CheckoutResult;
import com.chicamax.sentinella.payments.interfaces.rest.resources.CheckoutResource;
import com.chicamax.sentinella.payments.interfaces.rest.resources.PaymentResource;
import com.chicamax.sentinella.payments.interfaces.rest.resources.PlanResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentResourceAssembler {

    public PlanResource toResource(Plan plan) {
        return new PlanResource(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getPriceCents(),
                plan.getSetupPriceCents(),
                plan.getCurrency(),
                plan.getSensorLimit(),
                plan.getBillingPeriod()
        );
    }

    public PaymentResource toResource(Payment payment) {
        return toResource(payment, null);
    }

    public PaymentResource toResource(CheckoutResult checkoutResult) {
        return toResource(checkoutResult.payment(), checkoutResult.checkoutUrl());
    }

    public PaymentResource toResource(Payment payment, String checkoutUrl) {
        return new PaymentResource(
                payment.getId(),
                payment.getUserId(),
                payment.getPlanId(),
                payment.getStatus().name(),
                payment.getAmountCents(),
                payment.getCurrency(),
                checkoutUrl
        );
    }

    public CreateCheckoutCommand toCommand(UUID userId, CheckoutResource resource) {
        return new CreateCheckoutCommand(userId, resource.planId(), resource.customerEmail());
    }
}
