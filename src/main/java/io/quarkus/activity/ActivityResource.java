package io.quarkus.activity;

import io.quarkus.activity.github.GitHubService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;

@Path("/")
public class ActivityResource {

    @Inject
    GitHubActivitiesService gitHubActivitiesService;

    @Inject
    GitHubMonthlyStatsService gitHubMonthlyStatsService;

    @Inject
    GitHubOpenPrQueueService gitHubOpenPrQueueService;

    @Inject
    GitHubService gitHubService;

    @Inject
    GitHubDailyStatusService gitHubDailyStatusService;

    @Inject
    Template activities;

    @Inject
    Template monthlyStats;

    @Inject
    Template openPrQueue;

    @Inject
    Template dailyStatus;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getActivities() throws IOException {

        return activities.data(
                "logins", gitHubService.getLogins(),
                "activities", gitHubActivitiesService.getGitHubActivities(),
                "updated", gitHubActivitiesService.getUpdated(),
                "limit", gitHubService.getLimit());
    }

    @GET
    @Path("/monthly-stats")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getMonthlyStats() throws IOException {
        return monthlyStats.data(
                "logins", gitHubService.getLogins(),
                "stats", gitHubMonthlyStatsService.getMonthlyStats(),
                "colors", Map.of("rsvoboda", "RED", "mjurc", "BLUE")
        );
    }

    @GET
    @Path("/open-pr-queue")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getOpenPrsQueue() throws IOException {
        return openPrQueue.data(
                "logins", gitHubService.getLogins(),
                "organization", gitHubOpenPrQueueService.getOpenPrQueueInOrganization()
        );
    }

    @GET
    @Path("/daily-status")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDailyStatus() throws IOException {
        return dailyStatus.data(
                "logins", gitHubService.getLogins(),
                "repositories", gitHubDailyStatusService.getRepositoriesWithDailyStatus()
        );
    }
}
