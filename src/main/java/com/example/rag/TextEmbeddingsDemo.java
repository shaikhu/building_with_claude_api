package com.example.rag;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextEmbeddingsDemo {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    /** Sends all inputs in one batch request and returns one embedding vector per input. */
    public static List<List<Double>> generateEmbeddings(List<String> inputs, String inputType) throws Exception {
        String json = MAPPER.writeValueAsString(Map.of(
                "input", inputs,
                "model", "voyage-4",
                "input_type", inputType
        ));

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.voyageai.com/v1/embeddings"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + System.getenv("VOYAGE_API_KEY"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        List<List<Double>> embeddings = new ArrayList<>();
        MAPPER.readTree(response.body())
                .path("data")
                .forEach(item -> {
                    var embedding = new ArrayList<Double>();
                    item.path("embedding").forEach(v -> embedding.add(v.asDouble()));
                    embeddings.add(embedding);
                });
        return embeddings;
    }

    /** Convenience wrapper for a single input. */
    public static List<Double> generateEmbedding(String input, String inputType) throws Exception {
        return generateEmbeddings(List.of(input), inputType).get(0);
    }

    public static void main(String... args) throws Exception {
        String text = Files.readString(Paths.get(ChunkingDemo.class.getResource("report.md").toURI()));
        List<String> chunks = ChunkingDemo.chunkBySection(text);
        generateEmbeddings(chunks, "document").forEach(System.out::println);
    }
}
