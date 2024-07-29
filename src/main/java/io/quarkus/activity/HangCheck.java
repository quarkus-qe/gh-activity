package io.quarkus.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import java.time.LocalDateTime;

@ApplicationScoped
@Liveness
/*
  GraphQL client may stop to retrieve data properly (https://github.com/quarkusio/quarkus/issues/42029),
  but restarting the app solves the problem.

  This class creates a liveness endpoint and checks, that PR queue was updated at least two hours ago.
  If it wasn't (ie last 12 requests failed), the next liveness probe will fail and the pod restarted
 */
public class HangCheck implements HealthCheck {

    @Inject
    GitHubOpenPrQueueService prService;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("GraphQL updates check");
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);

        builder.up();
        prService.getLastUpdated()
                .filter(lastUpdated -> lastUpdated.isBefore(twoHoursAgo))
                .ifPresent(lastUpdated -> {
                    builder.down();
                    builder.withData("lastUpdate", lastUpdated.toString());
                });

        return builder.build();
    }
}
