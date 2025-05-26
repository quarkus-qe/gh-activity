package io.quarkus.activity;

import io.quarkus.activity.github.GitHubService;
import io.quarkus.activity.model.MonthlyStats;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;

@ApplicationScoped
public class GitHubMonthlyStatsService {

    @Inject
    GitHubService gitHubService;

    private volatile MonthlyStats monthlyStats;

    @ConfigProperty(name = "activity.start", defaultValue = "2020-01-01")
    LocalDate statsStart;

    @Scheduled(every = "6H")
    public void updateMonthlyStats() throws IOException {
        monthlyStats = buildMonthlyStats();
    }

    public MonthlyStats getMonthlyStats() throws IOException {
        return monthlyStats == null ? new MonthlyStats() : monthlyStats;
    }

    private MonthlyStats buildMonthlyStats() throws IOException {
        return gitHubService.getMonthlyStats(statsStart);
    }
}
