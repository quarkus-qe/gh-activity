package io.quarkus.activity.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.function.Predicate;

@RegisterForReflection
public class GitHubActivities {

    private static final String OPEN_STATE = "OPEN";

    public User user;
    public List<ActivityEntry> pullRequestsActivities;
    public List<ActivityEntry> issuesActivities;
    public List<ActivityEntry> issueCommentsActivities;
    public long openPullRequestsCount;
    public long openIssuesCount;

    public GitHubActivities(User user, List<ActivityEntry> pullRequestsActivities, List<ActivityEntry> issuesActivities,
                            List<ActivityEntry> issueCommentsActivities) {
        this.user = user;
        this.pullRequestsActivities = pullRequestsActivities;
        this.issuesActivities = issuesActivities;
        this.issueCommentsActivities = issueCommentsActivities;
        this.openPullRequestsCount = pullRequestsActivities.stream().filter(hasOpenState()).count();
        this.openIssuesCount = issuesActivities.stream().filter(hasOpenState()).count();
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

    private static final Predicate<ActivityEntry> hasOpenState() {
        return a -> OPEN_STATE.equalsIgnoreCase(a.state);
    }
}
