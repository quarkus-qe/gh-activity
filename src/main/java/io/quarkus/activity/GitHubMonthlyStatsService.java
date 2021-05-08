package io.quarkus.activity;

import io.quarkus.activity.github.GitHubService;
import io.quarkus.activity.model.MonthlyStats;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
        MonthlyStats localStats = monthlyStats;
        if (localStats == null) {
            synchronized (this) {
                localStats = monthlyStats;
                if (monthlyStats == null) {
                    monthlyStats = localStats = buildMonthlyStats();
                }
            }
        }
        return localStats;
    }

    private MonthlyStats buildMonthlyStats() throws IOException {
        return gitHubService.getMonthlyStats(statsStart);
    }
}
