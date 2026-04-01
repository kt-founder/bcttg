package com.bcttg.module.report.dto;

import java.util.List;

public class ReportOverviewResponse {
    private final List<ReportSummaryCardResponse> summaryCards;
    private final List<ReportTrendSeriesItemResponse> trendSeries;
    private final List<ReportUserActivityItemResponse> userActivity;
    private final List<ReportTopContentItemResponse> topContent;

    public ReportOverviewResponse(
        List<ReportSummaryCardResponse> summaryCards,
        List<ReportTrendSeriesItemResponse> trendSeries,
        List<ReportUserActivityItemResponse> userActivity,
        List<ReportTopContentItemResponse> topContent
    ) {
        this.summaryCards = summaryCards;
        this.trendSeries = trendSeries;
        this.userActivity = userActivity;
        this.topContent = topContent;
    }

    public List<ReportSummaryCardResponse> getSummaryCards() {
        return summaryCards;
    }

    public List<ReportTrendSeriesItemResponse> getTrendSeries() {
        return trendSeries;
    }

    public List<ReportUserActivityItemResponse> getUserActivity() {
        return userActivity;
    }

    public List<ReportTopContentItemResponse> getTopContent() {
        return topContent;
    }
}
