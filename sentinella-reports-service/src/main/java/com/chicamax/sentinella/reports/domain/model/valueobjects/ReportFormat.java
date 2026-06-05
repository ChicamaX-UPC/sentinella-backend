package com.chicamax.sentinella.reports.domain.model.valueobjects;

public enum ReportFormat {
    PDF,
    EXCEL;

    public String fileExtension() {
        return switch (this) {
            case PDF -> "pdf";
            case EXCEL -> "xlsx";
        };
    }
}
