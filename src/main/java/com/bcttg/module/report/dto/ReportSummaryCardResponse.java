package com.bcttg.module.report.dto;

public class ReportSummaryCardResponse {
    private final String id;
    private final String title;
    private final String value;
    private final double change;
    private final String period;
    private final String iconKey;

    public ReportSummaryCardResponse(String id, String title, String value, double change, String period, String iconKey) {
        this.id = id;
        this.title = title;
        this.value = value;
        this.change = change;
        this.period = period;
        this.iconKey = iconKey;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public double getChange() {
        return change;
    }

    public String getPeriod() {
        return period;
    }

    public String getIconKey() {
        return iconKey;
    }
}
