package com.chicamax.sentinella.monitoring.domain.prediction;

/** Parámetros del expediente Chicama Norte (alineados al gemelo digital). */
public final class ChicamaBasinConstants {

    public static final double BASIN_AREA_M2 = 17_000;
    public static final double CATCHMENT_RATIO = 2.8;
    public static final double RELAVE_MAX_OPERATING_M = 785;

    private ChicamaBasinConstants() {
    }

    /**
     * Subida de cota (m) por intensidad pluvial (mm/h) durante {@code hours}.
     * Modelo simplificado del balance hídrico del vaso.
     */
    public static double levelRiseFromRainMmPerHour(double rainMmPerHour, double hours) {
        if (rainMmPerHour <= 0 || hours <= 0) {
            return 0;
        }
        return (rainMmPerHour / 1000.0) * CATCHMENT_RATIO * hours * 0.012;
    }
}
