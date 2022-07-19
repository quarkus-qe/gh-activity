package io.quarkus.activity;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.activity.jenkins.JenkinsIngestionService;
import io.quarkus.activity.model.DailyStatusRepositories;

@ApplicationScoped
public class GitHubDailyStatusService {

    @Inject
    public DailyStatusRepositories dailyStatusRepositories;

    @Inject
    public JenkinsIngestionService jenkinsStatus;

    public Map<String, String> getRepositoriesWithDailyStatus() {
        return new TreeMap<>(dailyStatusRepositories.repositories());
    }

    public List<JenkinsIngestionService.Entry> getJenkinsJobsWithDailyStatus() {
        return jenkinsStatus.getJenkinsJobLatestStatus();
    }
}
