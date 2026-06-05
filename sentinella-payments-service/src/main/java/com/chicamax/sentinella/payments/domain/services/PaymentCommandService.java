package com.chicamax.sentinella.payments.domain.services;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import com.chicamax.sentinella.payments.domain.model.commands.CreateCheckoutCommand;
import java.util.List;
import java.util.UUID;

public interface PaymentCommandService {
    List<Plan> listPlans();

    Payment createCheckout(CreateCheckoutCommand command);

    Payment confirmPayment(UUID paymentId);
}
