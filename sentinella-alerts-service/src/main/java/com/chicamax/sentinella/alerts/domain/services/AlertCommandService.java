package com.chicamax.sentinella.alerts.domain.services;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.commands.UpdateAlertCommand;

public interface AlertCommandService {
    Alert create(CreateAlertCommand command);

    Alert update(UpdateAlertCommand command);
}
