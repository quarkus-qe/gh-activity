package io.quarkus.activity.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

@RegisterForReflection
public class ActivityEntry {
    public Instant created;
    public String repository;
    public String url;
    public String state;
    public String labels;

    public ActivityEntry(Instant created, String repository, String url, String state, String labels) {
        this.created = created;
        this.repository = repository;
        this.url = url;
        this.state = state;
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "ActivityEntry{" +
                "created=" + created +
                ", repository='" + repository + '\'' +
                ", url='" + url + '\'' +
                ", state='" + state + '\'' +
                ", labels='" + labels + '\'' +
                '}';
    }
}
