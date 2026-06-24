package com.chicamax.sentinella.payments.domain.services;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import com.chicamax.sentinella.payments.domain.model.commands.CreateCheckoutCommand;
import com.chicamax.sentinella.payments.domain.model.valueobjects.CheckoutResult;
import java.util.List;
import java.util.UUID;

public interface PaymentCommandService {
    List<Plan> listPlans();

    CheckoutResult createCheckout(CreateCheckoutCommand command);

    Payment confirmPayment(UUID paymentId, String stripeSubscriptionId);

    String createPortalSession(UUID userId);

    void cancelSubscriptionByStripeId(String stripeSubscriptionId);
}
