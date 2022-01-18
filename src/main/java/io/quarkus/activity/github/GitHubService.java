package io.quarkus.activity.github;

import io.quarkus.activity.model.ActivityEntry;
import io.quarkus.activity.model.GitHubActivities;
import io.quarkus.activity.model.MonthlyStats;
import io.quarkus.activity.model.OpenPullRequestsQueueByRepositories;
import io.quarkus.activity.model.PullRequestWithReviewers;
import io.quarkus.activity.model.Repository;
import io.quarkus.activity.model.User;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.activity.graphql.GraphQLClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class GitHubService {

    @Inject
    @RestClient
    GraphQLClient graphQLClient;

    private final String token;

    @ConfigProperty(name = "activity.logins", defaultValue = "rsvoboda,mjurc,pjgg,kshpak,jsmrcka,fedinskiy,Sgitario")
    List<String> logins;

    @ConfigProperty(name = "activity.limit", defaultValue = "100")
    int limit;

    @Inject
    public GitHubService(
            @ConfigProperty(name = "activity.token") String token) {
        this.token = "Bearer " + token;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public List<String> getLogins() {
        return logins;
    }

    public List<GitHubActivities> getGitHubActivities() throws IOException {
        String query = Templates.latestActivity(logins, limit).render();
        JsonObject response = graphQLClient.graphql(token, new JsonObject().put("query", query));
        handleErrors(response);

        JsonObject dataJson = response.getJsonObject("data");

        List<GitHubActivities> activities = new ArrayList<>(logins.size());
        for (String login : logins) {
            JsonObject activityJson = dataJson.getJsonObject(login);

            GitHubActivities userActivities = extractGitHubActivities(activityJson);
            activities.add(userActivities);
        }
        return activities;
    }

    private GitHubActivities extractGitHubActivities(JsonObject activityJson) {
        User user = new User(
                activityJson.getString("login"),
                activityJson.getString("name"),
                activityJson.getString("url"),
                activityJson.getString("avatarUrl")
        );
        JsonArray pullRequestsNodesJson = activityJson.getJsonObject("pullRequests").getJsonArray("nodes");
        JsonArray issuesNodesJson = activityJson.getJsonObject("issues").getJsonArray("nodes");
        JsonArray issueCommentsNodesJson = activityJson.getJsonObject("issueComments").getJsonArray("nodes");

        List<ActivityEntry> pullRequestsActivities = extractActivities(pullRequestsNodesJson);
        List<ActivityEntry> issuesActivities = extractActivities(issuesNodesJson);
        List<ActivityEntry> issueCommentsActivities = extractActivities(issueCommentsNodesJson);

        return new GitHubActivities(user, pullRequestsActivities, issuesActivities, issueCommentsActivities);
    }

    private List<ActivityEntry> extractActivities(JsonArray nodesJson) {
        List<ActivityEntry> activities= new ArrayList<>(nodesJson.size());
        for (Object nodeJson : nodesJson) {
            JsonObject objectJson = (JsonObject) nodeJson;
            ActivityEntry activity = new ActivityEntry(
                    objectJson.getInstant("createdAt"),
                    objectJson.getJsonObject("repository").getString("nameWithOwner"),
                    objectJson.getString("url"),
                    objectJson.getString("state"),
                    extractLabels(objectJson)
            );
            activities.add(activity);
        }
        return activities;
    }

    public MonthlyStats getMonthlyStats(LocalDate statsStart) throws IOException {
        MonthlyStats stats = new MonthlyStats();
        stats.updated = LocalDateTime.now();
        for (String login : logins) {
            stats.quarkusioPRs.put(login, new LinkedList<>());
            stats.quarkusioIssues.put(login, new LinkedList<>());
            stats.quarkusqePRs.put(login, new LinkedList<>());
            stats.quarkusqeReviews.put(login, new LinkedList<>());
            stats.quarkusqeMergedPRs.put(login, new LinkedList<>());
        }

        LocalDate start = statsStart;
        LocalDate stopTime = LocalDate.now().withDayOfMonth(2);

        while (start.isBefore(stopTime)) {
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            String timeWindow = start + ".." + end;
            stats.months.add(FORMATTER.format(start));

            String query = Templates.monthlyActivity(logins, timeWindow).render();
            JsonObject response = graphQLClient.graphql(token, new JsonObject().put("query", query));
            handleErrors(response);

            JsonObject dataJson = response.getJsonObject("data");
            ArrayList<String> mergers = getPrMergers(timeWindow);

            for (String login : logins) {
                stats.quarkusioPRs.get(login).add(dataJson.getJsonObject(login + "_prs_quarkusio").getString("issueCount"));
                stats.quarkusioIssues.get(login).add(dataJson.getJsonObject(login + "_issues_quarkusio").getString("issueCount"));
                stats.quarkusqePRs.get(login).add(dataJson.getJsonObject(login + "_prs_quarkus_qe").getString("issueCount"));
                stats.quarkusqeReviews.get(login).add(dataJson.getJsonObject(login + "_reviews_quarkus_qe").getString("issueCount"));
                stats.quarkusqeMergedPRs.get(login).add(String.valueOf(mergers.stream().filter(mergerLogin -> mergerLogin.equals(login)).count()));
            }

            start = start.plusMonths(1);
        }
        return stats;
    }

    private ArrayList<String> getPrMergers(String timeWindow) throws IOException {
        JsonObject response;
        String cursor = null;
        ArrayList<String> mergers = new ArrayList<>();
        do {
            response = graphQLClient.graphql(token, new JsonObject().put("query", Templates.monthlyMergedPRs(timeWindow, cursor).render()));
            handleErrors(response);

            JsonArray nodesJsonArray = response.getJsonObject("data").getJsonObject("search").getJsonArray("nodes");
            nodesJsonArray.stream().map(item -> ((JsonObject) item).getJsonObject("mergedBy").getString("login")).forEach(mergers::add);

            JsonObject pageInfoJson = response.getJsonObject("data").getJsonObject("search").getJsonObject("pageInfo");
            cursor = pageInfoJson.getBoolean("hasNextPage") ? pageInfoJson.getString("endCursor") : null;
        } while (cursor != null);
        return mergers;
    }

    public OpenPullRequestsQueueByRepositories getOpenPrQueueInOrganization(String organizationName) throws IOException {
        OpenPullRequestsQueueByRepositories organization = new OpenPullRequestsQueueByRepositories();

        List<Repository> repositories = getRepositoriesInOrganization(organizationName);

        String query = Templates.openPullRequestsInRepositories(repositories, organizationName, limit).render();
        JsonObject response = graphQLClient.graphql(token, new JsonObject().put("query", query));
        handleErrors(response);

        JsonObject dataJson = response.getJsonObject("data");

        for (Repository repository : repositories) {
            JsonArray pullRequestsNodesJson = dataJson.getJsonObject(repository.id + "_open_prs").getJsonObject("pullRequests").getJsonArray("nodes");

            List<PullRequestWithReviewers> pullRequests = pullRequestsNodesJson.stream().map(prItem -> {
                JsonObject prJsonItem = (JsonObject) prItem;
                Instant createdAt = prJsonItem.getInstant("createdAt");
                String title = prJsonItem.getString("title");
                String repositoryUrl = prJsonItem.getString("url");
                String author = prJsonItem.getJsonObject("author").getString("login");
                Set<String> reviewers = new HashSet<>();
                // Get from requested reviews (when reviewers have not started to look into the PR yet)
                prJsonItem.getJsonObject("reviewRequests").getJsonArray("nodes")
                        .stream().map(item -> ((JsonObject) item).getJsonObject("requestedReviewer").getString("login"))
                        .forEach(reviewers::add);
                // Get from already started reviews
                prJsonItem.getJsonObject("reviews").getJsonArray("nodes")
                        .stream().map(item -> ((JsonObject) item).getJsonObject("author").getString("login"))
                        .filter(login -> !StringUtils.equals(login, author)) // discard author comments from reviews
                        .forEach(reviewers::add);
                return new PullRequestWithReviewers(createdAt, title, repositoryUrl, author, reviewers.stream().collect(
                        Collectors.joining(", ")));
            }).collect(Collectors.toList());

            // Filter out repositories without pull requests
            if (!pullRequests.isEmpty()) {
                organization.repositories.put(repository.name, pullRequests);
            }
        }

        organization.updated = LocalDateTime.now();

        return organization;
    }

    private List<Repository> getRepositoriesInOrganization(String organization) throws IOException {
        String query = Templates.repositoriesByOrganization(organization, limit).render();
        JsonObject response = graphQLClient.graphql(token, new JsonObject().put("query", query));
        handleErrors(response);

        JsonArray dataJson = response.getJsonObject("data").getJsonObject("search").getJsonArray("edges");
        return dataJson.stream()
                .map(item -> ((JsonObject) item).getJsonObject("node").getString("name"))
                .map(Repository::new)
                .collect(Collectors.toList());
    }

    @CheckedTemplate
    private static class Templates {
        public static native TemplateInstance latestActivity(Collection<String> logins, Integer limit);
        public static native TemplateInstance monthlyActivity(Collection<String> logins, String timeWindow);
        public static native TemplateInstance monthlyMergedPRs(String timeWindow, String cursor);
        public static native TemplateInstance repositoriesByOrganization(String organization, Integer limit);
        public static native TemplateInstance openPullRequestsInRepositories(Collection<Repository> repositories, String organization, Integer limit);
    }

    private String extractLabels(JsonObject objectJson) {
        String labels = "";
        if (objectJson.containsKey("labels")) {
            labels = objectJson.getJsonObject("labels").getJsonArray("nodes")
                    .stream()
                    .map(json -> ((JsonObject) json).getString("name"))
                    .collect(Collectors.joining(","));
        }

        return labels;
    }

    private void handleErrors(JsonObject response) throws IOException {
        JsonArray errors = response.getJsonArray("errors");
        if (errors != null) {
            // Checking if there are any errors different from NOT_FOUND
            for (int k = 0; k < errors.size(); k++) {
                JsonObject error = errors.getJsonObject(k);
                if (!"NOT_FOUND".equals(error.getString("type"))) {
                    throw new IOException(error.toString());
                }
            }
        }
    }

}