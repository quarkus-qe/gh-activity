package io.quarkus.activity.model;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PullRequestWithReviewers {
    public Instant created;
    public String title;
    public String url;
    public String shortUrl;
    public String author;
    public String reviewers;

    public PullRequestWithReviewers(Instant created, String title, String url, String author, String reviewers) {
        this.created = created;
        this.title = title;
        this.url = url;
        this.author = author;
        this.reviewers = reviewers;
        this.shortUrl = url.substring(url.lastIndexOf("/") + 1);
    }

    @Override
    public String toString() {
        return "PullRequestWithReviewers{" +
                "created=" + created +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", author='" + author + '\'' +
                ", reviewers='" + reviewers + '\'' +
                '}';
    }
}
