package io.quarkus.activity.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

@RegisterForReflection
public class ActivityEntry {
    public Instant created;
    public String repository;
    public String url;

    public ActivityEntry(Instant created, String repository, String url) {
        this.created = created;
        this.repository = repository;
        this.url = url;
    }

    @Override
    public String toString() {
        return "ActivityEntry{" +
                "created=" + created +
                ", repository='" + repository + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
