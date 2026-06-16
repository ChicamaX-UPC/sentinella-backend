package com.chicamax.sentinella.alerts.infrastructure.notifications;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.infrastructure.integration.IamPushTargetClient;
import com.chicamax.sentinella.alerts.infrastructure.integration.MonitoringInternalClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExpoPushDispatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpoPushDispatchService.class);

    private final HttpClient httpClient;
    private final MonitoringInternalClient monitoringInternalClient;
    private final IamPushTargetClient iamPushTargetClient;
    private final boolean pushEnabled;
    private final String expoAccessToken;

    public ExpoPushDispatchService(
            MonitoringInternalClient monitoringInternalClient,
            IamPushTargetClient iamPushTargetClient,
            @Value("${alerts.notifications.push.enabled:false}") boolean pushEnabled,
            @Value("${alerts.notifications.push.expo-access-token:}") String expoAccessToken
    ) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.monitoringInternalClient = monitoringInternalClient;
        this.iamPushTargetClient = iamPushTargetClient;
        this.pushEnabled = pushEnabled;
        this.expoAccessToken = expoAccessToken;
    }

    public void sendForAlert(Alert alert) {
        if (!pushEnabled || alert == null) {
            return;
        }
        UUID tailingDamId = monitoringInternalClient.resolveTailingDamId(alert.getNodeId());
        List<IamPushTargetClient.PushTarget> targets = iamPushTargetClient.findTargets(tailingDamId);
        if (targets.isEmpty()) {
            LOGGER.debug("Sin tokens push para tranque {} (alerta {})", tailingDamId, alert.getId());
            return;
        }

        String title = "Alerta " + alert.getSeverity().name();
        String body = "%s — valor %s".formatted(alert.getSensorType(), formatValue(alert.getTriggeredValue()));
        String payload = buildPayload(targets, title, body, alert);
        postToExpo(payload);
    }

    private void postToExpo(String payload) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("https://exp.host/--/api/v2/push/send"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload));
            if (expoAccessToken != null && !expoAccessToken.isBlank()) {
                builder.header("Authorization", "Bearer " + expoAccessToken);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                LOGGER.warn("Expo Push respondio {}: {}", response.statusCode(), response.body());
            }
        } catch (IOException ex) {
            LOGGER.warn("No se pudo enviar push Expo", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Envio push Expo interrumpido", ex);
        }
    }

    private static String buildPayload(
            List<IamPushTargetClient.PushTarget> targets,
            String title,
            String body,
            Alert alert
    ) {
        StringBuilder messages = new StringBuilder("[");
        for (int i = 0; i < targets.size(); i++) {
            if (i > 0) {
                messages.append(',');
            }
            IamPushTargetClient.PushTarget target = targets.get(i);
            messages.append("{\"to\":\"").append(escapeJson(target.token()))
                    .append("\",\"title\":\"").append(escapeJson(title))
                    .append("\",\"body\":\"").append(escapeJson(body))
                    .append("\",\"sound\":\"default\",\"priority\":\"high\",\"channelId\":\"alerts\",\"data\":{")
                    .append("\"alertId\":\"").append(alert.getId())
                    .append("\",\"nodeId\":\"").append(alert.getNodeId())
                    .append("\",\"severity\":\"").append(alert.getSeverity().name())
                    .append("\"}}");
        }
        messages.append(']');
        return messages.toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String formatValue(BigDecimal value) {
        return value == null ? "N/A" : value.stripTrailingZeros().toPlainString();
    }
}
