package com.bcttg.module.report.dto;

public class ReportTopContentItemResponse {
    private final String title;
    private final long views;
    private final double trend;

    public ReportTopContentItemResponse(String title, long views, double trend) {
        this.title = title;
        this.views = views;
        this.trend = trend;
    }

    public String getTitle() {
        return title;
    }

    public long getViews() {
        return views;
    }

    public double getTrend() {
        return trend;
    }
}
