package io.quarkus.activity;

import io.quarkus.activity.github.GitHubService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;

@Path("/")
public class ActivityResource {

    @Inject
    GitHubActivitiesService gitHubActivitiesService;

    @Inject
    GitHubMonthlyStatsService gitHubMonthlyStatsService;

    @Inject
    GitHubService gitHubService;

    @Inject
    Template activities;

    @Inject
    Template monthlyStats;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getActivities() throws IOException {

        return activities.data(
                "logins", gitHubService.getLogins(),
                "activities", gitHubActivitiesService.getGitHubActivities(),
                "updated", gitHubActivitiesService.getUpdated());
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
}
