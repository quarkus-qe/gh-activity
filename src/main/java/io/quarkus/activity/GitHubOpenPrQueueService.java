package io.quarkus.activity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.activity.github.GitHubClient;
import io.quarkus.activity.model.OpenPullRequestsQueueByRepositories;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class GitHubOpenPrQueueService {

    @Inject
    GitHubClient gitHubService;

    private volatile OpenPullRequestsQueueByRepositories openPrQueueInOrganization;

    @ConfigProperty(name = "activity.pull-requests.organization", defaultValue = "quarkus-qe")
    String quarkusQeOrganization;

    @Scheduled(every = "10m")
    public void updateOpenPrQueueInOrganization() throws IOException {
        openPrQueueInOrganization = buildOpenPrQueueInOrganization();
    }

    public OpenPullRequestsQueueByRepositories getOpenPrQueueInOrganization() throws IOException {
        OpenPullRequestsQueueByRepositories localStats = openPrQueueInOrganization;
        if (localStats == null) {
            synchronized (this) {
                localStats = openPrQueueInOrganization;
                if (openPrQueueInOrganization == null) {
                    openPrQueueInOrganization = localStats = buildOpenPrQueueInOrganization();
                }
            }
        }

        return localStats;
    }

    private OpenPullRequestsQueueByRepositories buildOpenPrQueueInOrganization() throws IOException {
        return gitHubService.getOpenPrQueueInOrganization(quarkusQeOrganization);
    }

    public synchronized Optional<LocalDateTime> getLastUpdated() {
        return Optional.ofNullable(openPrQueueInOrganization).map(queue -> queue.updated);
    }
}
