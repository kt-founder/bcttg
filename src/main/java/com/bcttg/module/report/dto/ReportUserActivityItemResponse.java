package com.bcttg.module.report.dto;

public class ReportUserActivityItemResponse {
    private final String name;
    private final long value;
    private final double activity;

    public ReportUserActivityItemResponse(String name, long value, double activity) {
        this.name = name;
        this.value = value;
        this.activity = activity;
    }

    public String getName() {
        return name;
    }

    public long getValue() {
        return value;
    }

    public double getActivity() {
        return activity;
    }
}
