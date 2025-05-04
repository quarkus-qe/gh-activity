package io.quarkus.activity;

import io.quarkus.activity.github.GitHubClient;
import io.quarkus.activity.model.GitHubActivities;
import io.quarkus.scheduler.Scheduled;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class GitHubActivitiesService {

    @Inject
    GitHubClient gitHubService;

    @Inject
    Storage storage;

    private volatile LocalDateTime updated;

    @Scheduled(every = "10m")
    public void updateActivities() throws IOException {
        storage.putActivities(buildGitHubActivities());
    }

    public List<GitHubActivities> getGitHubActivities() {
        return storage.getActivities();
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
