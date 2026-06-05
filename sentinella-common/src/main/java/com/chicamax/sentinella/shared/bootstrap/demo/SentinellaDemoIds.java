package com.chicamax.sentinella.shared.bootstrap.demo;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Identificadores estables compartidos entre microservicios para datos demo (coherencia con el monolito).
 */
public final class SentinellaDemoIds {

    public static final UUID TAILING_DAM_CHICAMA_NORTE =
            UUID.fromString("a1e8c0de-4b2a-4c1f-9f3d-8c7b6a5d4e3f");

    public static final UUID USER_ADMIN = UUID.fromString("b0000001-0001-4001-8001-000000000001");
    public static final UUID USER_MANAGER = UUID.fromString("b0000002-0002-4002-8002-000000000002");
    public static final UUID USER_OPERATOR = UUID.fromString("b0000003-0003-4003-8003-000000000003");
    public static final UUID USER_AUDIT = UUID.fromString("b0000004-0004-4004-8004-000000000004");

    public static final UUID NODE_WATER_LEVEL = UUID.fromString("c1000001-0001-4001-8001-000000000001");
    public static final UUID NODE_PRESSURE = UUID.fromString("c1000002-0002-4002-8002-000000000002");
    public static final UUID NODE_PLUVIOMETER = UUID.fromString("c1000003-0003-4003-8003-000000000003");

    public static final String DEMO_PASSWORD = "Sentinella2024!";

    /** IDs estables de los 40 nodos demo (Monitoring y Alerts deben usar el mismo helper). */
    public static UUID demoNodeId(int index) {
        if (index == 0) {
            return NODE_WATER_LEVEL;
        }
        if (index == 1) {
            return NODE_PRESSURE;
        }
        if (index == 2) {
            return NODE_PLUVIOMETER;
        }
        return UUID.nameUUIDFromBytes(("chicama-demo-node-" + index).getBytes(StandardCharsets.UTF_8));
    }

    private SentinellaDemoIds() {
    }
}
