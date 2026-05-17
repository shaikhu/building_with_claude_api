package com.example.engineering;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class HtmlReport {

    public void create(List<Result> results) {
        String html = getHtml(results);
        try {
            Files.write(Paths.get("results.html"), html.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHtml(List<Result> results) {

        int total = results.size();

        double avgScore = results.stream()
                .mapToDouble(r -> r.verdict().score())
                .average()
                .orElse(0.0);

        long passCount = results.stream()
                .filter(r -> r.verdict().score() >= 7)
                .count();

        double passRate = total == 0 ? 0 : (passCount * 100.0 / total);

        List<Map<String, Object>> rows = results.stream().map(result -> {
            double score = result.verdict().score();
            String inputs = "<b>height:</b>" + result.athlete().height() + "<br />"
                    + "<b>weight:</b>" + result.athlete().weight() + "<br />"
                    + "<b>goal:</b>" + result.athlete().goal() + "<br />"
                    + "<b>restrictions:</b>" + result.athlete().restrictions() + "<br />";

            return Map.<String, Object>of(
                    "scenario",   result.athlete().activity(),
                    "inputs",     inputs,
                    "solution",     result.solution(),
                    "scoreClass", score >= 8 ? "score-high" : score <= 5 ? "score-low" : "score-medium",
                    "score",      String.format("%.1f", score),
                    "reasoning",  result.verdict().reasoning()
            );
        }).toList();

        Template tmpl = Mustache.compiler().compile(
                new InputStreamReader(getClass().getResourceAsStream("report.mustache")));

        return tmpl.execute(Map.of(
                "total",    total,
                "avgScore", String.format("%.1f", avgScore),
                "passRate", String.format("%.1f", passRate),
                "rows",     rows
        ));
    }
}
