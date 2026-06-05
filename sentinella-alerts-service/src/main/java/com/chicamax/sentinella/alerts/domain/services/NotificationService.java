package com.chicamax.sentinella.alerts.domain.services;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;

public interface NotificationService {
    void send(Alert alert, AlertChannel[] channels);
}
