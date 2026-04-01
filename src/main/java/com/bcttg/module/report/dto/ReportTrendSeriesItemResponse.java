package com.bcttg.module.report.dto;

public class ReportTrendSeriesItemResponse {
    private final String label;
    private final long views;
    private final long edits;
    private final long logins;

    public ReportTrendSeriesItemResponse(String label, long views, long edits, long logins) {
        this.label = label;
        this.views = views;
        this.edits = edits;
        this.logins = logins;
    }

    public String getLabel() {
        return label;
    }

    public long getViews() {
        return views;
    }

    public long getEdits() {
        return edits;
    }

    public long getLogins() {
        return logins;
    }
}
