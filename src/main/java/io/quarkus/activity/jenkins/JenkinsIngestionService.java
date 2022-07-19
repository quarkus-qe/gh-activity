package io.quarkus.activity.jenkins;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;

@ApplicationScoped
public class JenkinsIngestionService {

    @Inject
    RssReader rssReader;

    @ConfigProperty(name = "jenkins.domain")
    String jenkinsDomain;

    @ConfigProperty(name = "jenkins.job")
    List<String> jobs;

    @ConfigProperty(name = "jenkins.build.success.badge")
    String buildSuccessBadge;

    @ConfigProperty(name = "jenkins.build.failure.badge")
    String buildFailureBadge;

    @ConfigProperty(name = "jenkins.build.aborted.badge")
    String buildAbortedBadge;

    public List<Entry> getJenkinsJobLatestStatus() {
        List<Entry> entries = new ArrayList<>();
        try {
            for(String job: jobs) {
                String jenkinsEndpoint = String.format("%s/job/%s/rssAll", jenkinsDomain, job);
                Stream<Item> jenkinsJobHistory = rssReader.read(jenkinsEndpoint);
                Optional<Entry> latest = jenkinsJobHistory.map(histItem -> {
                    String title = histItem.getTitle().get();
                    String pubDate = histItem.getPubDate().get();
                    String link = String.format("%s/job/%s", jenkinsDomain, job);
                    return new Entry(title, pubDate, link);
                }).sorted(Entry::compareTo).findFirst(); // latest history record
                if(!latest.isEmpty()) {
                    entries.add(latest.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }

    public class Entry implements Comparable<Entry> {

        private final Logger LOG = Logger.getLogger(Entry.class);
        private  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        public String title;
        public Date updated;
        public String link;
        public String badge;

        public Entry(String title, String updated, String link) {
            try {
                this.title = title;
                this.updated = sdf.parse(updated);
                this.link = link;
                badge = buildSuccessBadge;
                if(title.contains("broken")) {
                    badge = buildFailureBadge;
                }

                if(title.contains("aborted")) {
                    badge = buildAbortedBadge;
                }

            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }

        @Override
        public int compareTo(Entry next) {
            return next.updated.compareTo(updated);
        }
    }
}
