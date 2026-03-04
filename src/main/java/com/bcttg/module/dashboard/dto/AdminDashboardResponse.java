package com.bcttg.module.dashboard.dto;

import java.time.Instant;
import java.util.List;

public class AdminDashboardResponse {
    private final Summary summary;
    private final List<LabelValueItem> monthlyContent;
    private final List<LabelValueItem> contentDistribution;
    private final List<LabelValueItem> weeklyVisits;
    private final List<ActivityItem> recentActivities;
    private final List<StatusItem> systemStatuses;
    private final List<PendingItem> pendingItems;

    public AdminDashboardResponse(
        Summary summary,
        List<LabelValueItem> monthlyContent,
        List<LabelValueItem> contentDistribution,
        List<LabelValueItem> weeklyVisits,
        List<ActivityItem> recentActivities,
        List<StatusItem> systemStatuses,
        List<PendingItem> pendingItems
    ) {
        this.summary = summary;
        this.monthlyContent = monthlyContent;
        this.contentDistribution = contentDistribution;
        this.weeklyVisits = weeklyVisits;
        this.recentActivities = recentActivities;
        this.systemStatuses = systemStatuses;
        this.pendingItems = pendingItems;
    }

    public Summary getSummary() {
        return summary;
    }

    public List<LabelValueItem> getMonthlyContent() {
        return monthlyContent;
    }

    public List<LabelValueItem> getContentDistribution() {
        return contentDistribution;
    }

    public List<LabelValueItem> getWeeklyVisits() {
        return weeklyVisits;
    }

    public List<ActivityItem> getRecentActivities() {
        return recentActivities;
    }

    public List<StatusItem> getSystemStatuses() {
        return systemStatuses;
    }

    public List<PendingItem> getPendingItems() {
        return pendingItems;
    }

    public static class Summary {
        private final long totalPosts;
        private final long totalProfiles;
        private final long totalSongs;
        private final long totalAccounts;
        private final long viewsToday;
        private final long editsToday;

        public Summary(long totalPosts, long totalProfiles, long totalSongs, long totalAccounts, long viewsToday, long editsToday) {
            this.totalPosts = totalPosts;
            this.totalProfiles = totalProfiles;
            this.totalSongs = totalSongs;
            this.totalAccounts = totalAccounts;
            this.viewsToday = viewsToday;
            this.editsToday = editsToday;
        }

        public long getTotalPosts() {
            return totalPosts;
        }

        public long getTotalProfiles() {
            return totalProfiles;
        }

        public long getTotalSongs() {
            return totalSongs;
        }

        public long getTotalAccounts() {
            return totalAccounts;
        }

        public long getViewsToday() {
            return viewsToday;
        }

        public long getEditsToday() {
            return editsToday;
        }
    }

    public static class LabelValueItem {
        private final String label;
        private final long value;

        public LabelValueItem(String label, long value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public long getValue() {
            return value;
        }
    }

    public static class ActivityItem {
        private final String title;
        private final String actor;
        private final String detail;
        private final Instant createdAt;
        private final String status;

        public ActivityItem(String title, String actor, String detail, Instant createdAt, String status) {
            this.title = title;
            this.actor = actor;
            this.detail = detail;
            this.createdAt = createdAt;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getActor() {
            return actor;
        }

        public String getDetail() {
            return detail;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class StatusItem {
        private final String name;
        private final String value;
        private final String state;

        public StatusItem(String name, String value, String state) {
            this.name = name;
            this.value = value;
            this.state = state;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getState() {
            return state;
        }
    }

    public static class PendingItem {
        private final String title;
        private final String type;
        private final String createdBy;
        private final Instant createdAt;
        private final String status;

        public PendingItem(String title, String type, String createdBy, Instant createdAt, String status) {
            this.title = title;
            this.type = type;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public String getStatus() {
            return status;
        }
    }
}
