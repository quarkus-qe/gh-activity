package io.quarkus.activity;

import java.util.Map;
import java.util.TreeMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.activity.model.DailyStatusRepositories;

@ApplicationScoped
public class GitHubDailyStatusService {

    @Inject
    public DailyStatusRepositories dailyStatusRepositories;

    public Map<String, String> getRepositoriesWithDailyStatus() {
        return new TreeMap<>(dailyStatusRepositories.repositories());
    }
}
