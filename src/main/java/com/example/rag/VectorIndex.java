package com.example.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * In-memory vector store supporting cosine and euclidean distance search.
 */
public class VectorIndex {
    private final List<List<Double>> vectors = new ArrayList<>();
    private final List<Map<String, Object>> documents = new ArrayList<>();
    private Integer vectorDim = null;
    private final String distanceMetric;
    private final Function<String, List<Double>> embeddingFn;

    public VectorIndex() {
        this("cosine", null);
    }

    public VectorIndex(String distanceMetric, Function<String, List<Double>> embeddingFn) {
        if (!distanceMetric.equals("cosine") && !distanceMetric.equals("euclidean")) {
            throw new IllegalArgumentException("distanceMetric must be 'cosine' or 'euclidean'");
        }
        this.distanceMetric = distanceMetric;
        this.embeddingFn = embeddingFn;
    }

    /** Generates an embedding via embeddingFn then stores it alongside the document. */
    public void addDocument(Map<String, Object> document) {
        if (embeddingFn == null) throw new IllegalStateException("No embedding function provided");
        if (!document.containsKey("content")) throw new IllegalArgumentException("Document must contain 'content' key");
        addVector(embeddingFn.apply((String) document.get("content")), document);
    }

    /** Stores a pre-computed embedding alongside the document. */
    public void addVector(List<Double> vector, Map<String, Object> document) {
        if (!document.containsKey("content")) throw new IllegalArgumentException("Document must contain 'content' key");
        if (vectorDim == null) {
            vectorDim = vector.size();
        } else if (vector.size() != vectorDim) {
            throw new IllegalArgumentException(
                    "Inconsistent vector dimension. Expected " + vectorDim + ", got " + vector.size());
        }
        vectors.add(new ArrayList<>(vector));
        documents.add(document);
    }

    /** Embeds the query string via embeddingFn then searches. */
    public List<Map.Entry<Map<String, Object>, Double>> search(String query, int k) {
        if (embeddingFn == null) throw new IllegalStateException("No embedding function provided for string query");
        return search(embeddingFn.apply(query), k);
    }

    /** Returns the k nearest documents to queryVector, sorted by ascending distance. */
    public List<Map.Entry<Map<String, Object>, Double>> search(List<Double> queryVector, int k) {
        if (vectors.isEmpty()) return List.of();
        if (queryVector.size() != vectorDim) {
            throw new IllegalArgumentException(
                    "Query vector dimension mismatch. Expected " + vectorDim + ", got " + queryVector.size());
        }
        if (k <= 0) throw new IllegalArgumentException("k must be positive");

        var distances = new ArrayList<Map.Entry<Map<String, Object>, Double>>();
        for (int i = 0; i < vectors.size(); i++) {
            double dist = distanceMetric.equals("cosine")
                    ? cosineDistance(queryVector, vectors.get(i))
                    : euclideanDistance(queryVector, vectors.get(i));
            distances.add(Map.entry(documents.get(i), dist));
        }
        distances.sort(Map.Entry.comparingByValue());
        return distances.subList(0, Math.min(k, distances.size()));
    }

    public int size() {
        return vectors.size();
    }

    private double euclideanDistance(List<Double> v1, List<Double> v2) {
        double sum = 0;
        for (int i = 0; i < v1.size(); i++) {
            double diff = v1.get(i) - v2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private double cosineDistance(List<Double> v1, List<Double> v2) {
        double dot = 0, mag1 = 0, mag2 = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            mag1 += v1.get(i) * v1.get(i);
            mag2 += v2.get(i) * v2.get(i);
        }
        mag1 = Math.sqrt(mag1);
        mag2 = Math.sqrt(mag2);
        if (mag1 == 0 && mag2 == 0) return 0.0;
        if (mag1 == 0 || mag2 == 0) return 1.0;
        return 1.0 - Math.max(-1.0, Math.min(1.0, dot / (mag1 * mag2)));
    }
}
