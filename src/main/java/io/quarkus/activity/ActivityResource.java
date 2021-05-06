package io.quarkus.activity;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
public class ActivityResource {

    @Inject
    GitHubActivitiesService gitHubActivitiesService;

    @Inject
    Template activities;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getActivities() throws IOException {

        return activities.data(
                "activities", gitHubActivitiesService.getGitHubActivities(),
                "updated", gitHubActivitiesService.getUpdated());
    }

}
