package io.quarkus.activity.model;

import java.util.Map;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "activity.daily-status")
public interface DailyStatusRepositories {
    Map<String, String> repositories();
}
