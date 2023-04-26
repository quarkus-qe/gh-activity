package io.quarkus.activity.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.graalvm.options.OptionDescriptors;

import jakarta.ws.rs.core.AbstractMultivaluedMap;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RegisterForReflection
public class MonthlyStats {
    public LocalDateTime updated;
    public List<String> months = new LinkedList<>();
    public Map<String, List<String>> quarkusioPRs = new LinkedHashMap<>();
    public Map<String, List<String>> quarkusioIssues = new LinkedHashMap<>();
    public Map<String, List<String>> quarkusqePRs = new LinkedHashMap<>();
    public Map<String, List<String>> quarkusqeReviews = new LinkedHashMap<>();
    public Map<String, List<String>> quarkusqeMergedPRs = new LinkedHashMap<>();
}
