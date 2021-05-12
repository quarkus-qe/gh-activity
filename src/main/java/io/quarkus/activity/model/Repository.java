package io.quarkus.activity.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Repository {
    public String id;
    public String name;

    public Repository(String name) {
        this.name = name;
        this.id = name.replaceAll("-", "_");
    }

    @Override
    public String toString() {
        return "Repository{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
