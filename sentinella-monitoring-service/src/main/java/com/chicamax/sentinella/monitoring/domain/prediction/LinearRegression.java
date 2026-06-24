package com.chicamax.sentinella.monitoring.domain.prediction;

import java.util.List;

public final class LinearRegression {

    private final double slope;
    private final double intercept;

    private LinearRegression(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public static LinearRegression fit(List<Double> xValues, List<Double> yValues) {
        if (xValues == null || yValues == null || xValues.size() < 2 || xValues.size() != yValues.size()) {
            return new LinearRegression(0, yValues != null && !yValues.isEmpty() ? yValues.get(yValues.size() - 1) : 0);
        }
        int n = xValues.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = xValues.get(i);
            double y = yValues.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-12) {
            return new LinearRegression(0, yValues.get(n - 1));
        }
        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;
        return new LinearRegression(slope, intercept);
    }

    public double slope() {
        return slope;
    }

    public double intercept() {
        return intercept;
    }

    public double predict(double x) {
        return slope * x + intercept;
    }
}
