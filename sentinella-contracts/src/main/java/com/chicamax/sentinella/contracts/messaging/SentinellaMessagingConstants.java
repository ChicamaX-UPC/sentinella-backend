package com.chicamax.sentinella.contracts.messaging;

/**
 * Contrato único de topología RabbitMQ (EDA) compartido entre servicios desplegables.
 * Arquitectura orientada a eventos: intercambio directo + routing keys versionables.
 */
public final class SentinellaMessagingConstants {

    private SentinellaMessagingConstants() {
    }

    public static final String SENTINELLA_EXCHANGE = "sentinella";
    public static final String SENTINELLA_DLX_EXCHANGE = "sentinella.dlx";

    public static final String TELEMETRY_ROUTING = "telemetry";
    /** Tras persistir lectura en monitoring → evaluación de umbrales en alerts. */
    public static final String SENSOR_READING_PERSISTED_ROUTING = "sensor.reading.persisted";
    /** Replica en tiempo casi real para el WebSocket/STOMP del dashboard. */
    public static final String SENSOR_READING_REALTIME_ROUTING = "sensor.reading.realtime";
    public static final String THRESHOLD_EXCEEDED_ROUTING = "threshold.exceeded";
    public static final String ALERT_CREATED_ROUTING = "alert.created";
    public static final String ALERT_CLOSED_ROUTING = "alert.closed";
    public static final String ALERT_ACKNOWLEDGED_ROUTING = "alert.acknowledged";
    public static final String NODE_OFFLINE_ROUTING = "node.offline";
    public static final String ROUND_SYNCED_ROUTING = "round.synced";

    public static final String USER_REGISTERED_ROUTING = "user.registered";
    public static final String PAYMENT_COMPLETED_ROUTING = "payment.completed";
    public static final String SUBSCRIPTION_ACTIVATED_ROUTING = "subscription.activated";
    public static final String SUBSCRIPTION_CANCELLED_ROUTING = "subscription.cancelled";
    public static final String SENSOR_REGISTERED_ROUTING = "sensor.registered";
    public static final String ALERT_TRIGGERED_ROUTING = "alert.triggered";
    public static final String BLOCKCHAIN_REGISTER_ROUTING = "blockchain.register";
}
