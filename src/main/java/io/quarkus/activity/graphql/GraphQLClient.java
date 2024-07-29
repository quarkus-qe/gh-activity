package io.quarkus.activity.graphql;

import io.vertx.core.json.JsonObject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.HttpHeaders;

import static java.time.temporal.ChronoUnit.SECONDS;

@Retry(delay = 2, delayUnit = SECONDS)
@RegisterRestClient(baseUri = "https://api.github.com/graphql")
@Singleton
public interface GraphQLClient {

    @POST
    JsonObject graphql(@HeaderParam(HttpHeaders.AUTHORIZATION) String authentication, JsonObject query);
}
