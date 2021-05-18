package io.quarkus.activity;

import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import io.quarkus.activity.model.DailyStatusRepositories;

@ApplicationScoped
public class GitHubDailyStatusService {

    @Inject
    public DailyStatusRepositories dailyStatusRepositories;

    public Map<String, String> getRepositoriesWithDailyStatus() {
        return new TreeMap<>(dailyStatusRepositories.repositories());
    }
}
