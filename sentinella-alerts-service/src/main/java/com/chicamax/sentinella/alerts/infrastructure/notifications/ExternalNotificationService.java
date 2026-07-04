package com.chicamax.sentinella.alerts.infrastructure.notifications;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.services.NotificationService;
import com.chicamax.sentinella.shared.infrastructure.mail.PlainTextMailClient;
import com.chicamax.sentinella.shared.infrastructure.mail.SentinellaEmailTemplate;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExternalNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalNotificationService.class);

    private final HttpClient httpClient;

    private final boolean notificationsEnabled;
    private final boolean emailEnabled;
    private final String emailApiUrl;
    private final String emailApiKey;
    private final String emailFrom;
    private final String emailTo;

    private final boolean smsEnabled;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String smsFrom;
    private final String smsTo;
    private final ExpoPushDispatchService expoPushDispatchService;
    private final ObjectProvider<PlainTextMailClient> mailClient;
    private final String appUrl;

    public ExternalNotificationService(
            @Value("${alerts.notifications.enabled:false}") boolean notificationsEnabled,
            @Value("${alerts.notifications.email.enabled:false}") boolean emailEnabled,
            @Value("${alerts.notifications.email.api-url:https://api.sendgrid.com/v3/mail/send}") String emailApiUrl,
            @Value("${alerts.notifications.email.api-key:}") String emailApiKey,
            @Value("${alerts.notifications.email.from:}") String emailFrom,
            @Value("${alerts.notifications.email.to:}") String emailTo,
            @Value("${alerts.notifications.sms.enabled:false}") boolean smsEnabled,
            @Value("${alerts.notifications.sms.twilio.account-sid:}") String twilioAccountSid,
            @Value("${alerts.notifications.sms.twilio.auth-token:}") String twilioAuthToken,
            @Value("${alerts.notifications.sms.from:}") String smsFrom,
            @Value("${alerts.notifications.sms.to:}") String smsTo,
            ExpoPushDispatchService expoPushDispatchService,
            ObjectProvider<PlainTextMailClient> mailClient,
            @Value("${sentinella.app-url:https://sentinella-frontend.vercel.app}") String appUrl
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.notificationsEnabled = notificationsEnabled;
        this.emailEnabled = emailEnabled;
        this.emailApiUrl = emailApiUrl;
        this.emailApiKey = emailApiKey;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.smsEnabled = smsEnabled;
        this.twilioAccountSid = twilioAccountSid;
        this.twilioAuthToken = twilioAuthToken;
        this.smsFrom = smsFrom;
        this.smsTo = smsTo;
        this.expoPushDispatchService = expoPushDispatchService;
        this.mailClient = mailClient;
        this.appUrl = appUrl == null ? "https://sentinella-frontend.vercel.app" : appUrl.replaceAll("/$", "");
    }

    @Override
    public void send(Alert alert, AlertChannel[] channels) {
        if (!notificationsEnabled || alert == null || channels == null || channels.length == 0) {
            return;
        }
        Arrays.stream(channels)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(channel -> dispatchChannel(alert, channel));
    }

    private void dispatchChannel(Alert alert, AlertChannel channel) {
        try {
            if (channel == AlertChannel.EMAIL) {
                sendEmail(alert);
            } else if (channel == AlertChannel.SMS) {
                sendSms(alert);
            } else if (channel == AlertChannel.APP) {
                expoPushDispatchService.sendForAlert(alert);
            }
        } catch (Exception exception) {
            LOGGER.warn("No se pudo enviar notificacion {} para alerta {}", channel, alert.getId(), exception);
        }
    }

    private void sendEmail(Alert alert) throws IOException, InterruptedException {
        if (!emailEnabled || isBlank(emailTo)) {
            return;
        }

        PlainTextMailClient resendClient = mailClient.getIfAvailable();
        if (resendClient != null) {
            String text = "Se registró una alerta %s en el nodo %s. Sensor: %s. Valor: %s."
                    .formatted(
                            alert.getSeverity().name(),
                            alert.getNodeId(),
                            alert.getSensorType(),
                            formatValue(alert.getTriggeredValue())
                    );
            String htmlBody = SentinellaEmailTemplate.detailRow("Severidad", alert.getSeverity().name())
                    + SentinellaEmailTemplate.detailRow("Nodo", alert.getNodeId().toString())
                    + SentinellaEmailTemplate.detailRow("Sensor", alert.getSensorType())
                    + SentinellaEmailTemplate.detailRow("Valor", formatValue(alert.getTriggeredValue()));
            String html = SentinellaEmailTemplate.render(
                    "Alerta operativa",
                    htmlBody,
                    "Ver alerta",
                    appUrl + "/alerts/" + alert.getId()
            );
            resendClient.sendRich(
                    emailTo.trim(),
                    "Alerta %s — Sentinella".formatted(alert.getSeverity().name()),
                    text,
                    html
            );
            return;
        }

        if (isBlank(emailApiKey) || isBlank(emailFrom)) {
            return;
        }

        String payload = """
                {
                  "personalizations":[{"to":[{"email":"%s"}]}],
                  "from":{"email":"%s"},
                  "subject":"Alerta %s en nodo %s",
                  "content":[{"type":"text/plain","value":"Severidad: %s | Valor: %s | Sensor: %s"}]
                }
                """.formatted(
                escapeJson(emailTo),
                escapeJson(emailFrom),
                escapeJson(alert.getId().toString()),
                escapeJson(alert.getNodeId().toString()),
                escapeJson(alert.getSeverity().name()),
                escapeJson(formatValue(alert.getTriggeredValue())),
                escapeJson(alert.getSensorType())
        );

        HttpRequest request = HttpRequest.newBuilder(URI.create(emailApiUrl))
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", "Bearer " + emailApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IOException("SendGrid respondio " + response.statusCode());
        }
    }

    private void sendSms(Alert alert) throws IOException, InterruptedException {
        if (!smsEnabled || isBlank(twilioAccountSid) || isBlank(twilioAuthToken) || isBlank(smsFrom) || isBlank(smsTo)) {
            return;
        }

        String endpoint = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json".formatted(twilioAccountSid);
        String messageBody = "Alerta %s | Nodo %s | Severidad %s | Valor %s"
                .formatted(alert.getId(), alert.getNodeId(), alert.getSeverity(), formatValue(alert.getTriggeredValue()));
        String form = "From=%s&To=%s&Body=%s".formatted(
                encode(smsFrom),
                encode(smsTo),
                encode(messageBody)
        );
        String auth = java.util.Base64.getEncoder().encodeToString(
                (twilioAccountSid + ":" + twilioAuthToken).getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IOException("Twilio respondio " + response.statusCode());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String formatValue(BigDecimal value) {
        return value == null ? "N/A" : value.stripTrailingZeros().toPlainString();
    }
}
