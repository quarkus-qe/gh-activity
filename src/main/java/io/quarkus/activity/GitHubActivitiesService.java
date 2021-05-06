package io.quarkus.activity;

import io.quarkus.activity.github.GitHubService;
import io.quarkus.activity.model.ActivityEntry;
import io.quarkus.activity.model.GitHubActivities;
import io.quarkus.activity.model.User;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GitHubActivitiesService {

    @Inject
    GitHubService gitHubService;

    private volatile List<GitHubActivities> ghActivities;

    private volatile LocalDateTime updated;

    @Scheduled(every = "6H")
    public void updateActivities() throws IOException {
        ghActivities = buildGitHubActivities();
    }

    public List<GitHubActivities> getGitHubActivities() throws IOException {
        List<GitHubActivities> localGhActivities = ghActivities;
        if (localGhActivities == null) {
            synchronized (this) {
                localGhActivities = ghActivities;
                if (ghActivities == null) {
                    ghActivities = localGhActivities = buildGitHubActivities();
                }
            }
        }
        return localGhActivities;
    }

    // Call this method after `getGitHubActivities`
    public LocalDateTime getUpdated() {
        return updated;
    }

    private List<GitHubActivities> buildGitHubActivities() throws IOException {
        // TODO aggregated info
        updated = LocalDateTime.now();
        return gitHubService.getGitHubActivities();
    }
}
