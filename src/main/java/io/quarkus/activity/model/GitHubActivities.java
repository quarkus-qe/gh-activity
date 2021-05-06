package io.quarkus.activity.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class GitHubActivities {
    public User user;
    public List<ActivityEntry> pullRequestsActivities;
    public List<ActivityEntry> issuesActivities;
    public List<ActivityEntry> issueCommentsActivities;

    public GitHubActivities(User user, List<ActivityEntry> pullRequestsActivities, List<ActivityEntry> issuesActivities,
                            List<ActivityEntry> issueCommentsActivities) {
        this.user = user;
        this.pullRequestsActivities = pullRequestsActivities;
        this.issuesActivities = issuesActivities;
        this.issueCommentsActivities = issueCommentsActivities;
    }

    @Override
    public String toString() {
        return "GitHubActivities{" +
                "user=" + user +
                ", pullRequestsActivities=" + pullRequestsActivities +
                ", issuesActivities=" + issuesActivities +
                ", issueCommentsActivities=" + issueCommentsActivities +
                '}';
    }
}
