package io.quarkus.activity.github;

import io.quarkus.activity.model.ActivityEntry;
import io.quarkus.activity.model.GitHubActivities;
import io.quarkus.activity.model.User;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.activity.graphql.GraphQLClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class GitHubService {

    @Inject
    @RestClient
    GraphQLClient graphQLClient;

    private final String token;

    @ConfigProperty(name = "activity.logins", defaultValue = "rsvoboda,mjurc,pjgg,Sgitario,kshpak,jsmrcka")
    List<String> logins;

    @ConfigProperty(name = "activity.limit", defaultValue = "100")
    int limit;

    @Inject
    public GitHubService(
            @ConfigProperty(name = "activity.token") String token) {
        this.token = "Bearer " + token;
        System.out.println("RS >> " + token);
    }


    public List<GitHubActivities> getGitHubActivities() throws IOException {
        String query = Templates.latestActivity(logins, limit).render();
        JsonObject response = graphQLClient.graphql(token, new JsonObject().put("query", query));
        handleErrors(response);

        JsonObject dataJson = response
                .getJsonObject("data");

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
            ActivityEntry activity = new ActivityEntry(
                    ((JsonObject) nodeJson).getInstant("createdAt"),
                    ((JsonObject) nodeJson).getJsonObject("repository").getString("nameWithOwner"),
                    ((JsonObject) nodeJson).getString("url")
            );
            activities.add(activity);
        }
        return activities;
    }


    @CheckedTemplate
    private static class Templates {
        public static native TemplateInstance latestActivity(Collection<String> logins, Integer limit);
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