package io.quarkus.activity;

import io.quarkus.activity.model.GitHubActivities;
import io.quarkus.activity.model.MonthlyStats;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class Storage {
    private volatile List<GitHubActivities> ghActivities = new ArrayList<>();
    private volatile MonthlyStats monthlyStats = new MonthlyStats();

    public synchronized void addActivities(List<GitHubActivities> activities) {
        this.ghActivities.addAll(activities);
    }

    public synchronized void putActivities(List<GitHubActivities> activities) {
        this.ghActivities = new ArrayList<>(activities);
    }

    public synchronized List<GitHubActivities> getActivities() {
        return new ArrayList<>(this.ghActivities);
    }

    public synchronized void putStats(MonthlyStats stats) {
        this.monthlyStats = stats;
    }

    public synchronized MonthlyStats getStats() {
        return this.monthlyStats;
    }
}
