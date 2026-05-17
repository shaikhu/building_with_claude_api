package com.example.rag;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class RagWorkflowDemo {

    public static void main(String... args) throws Exception {
        String text = Files.readString(Paths.get(RagWorkflowDemo.class.getResource("report.md").toURI()));

        // 1. Chunk text by section
        List<String> chunks = ChunkingDemo.chunkBySection(text);
        System.out.println("Chunks: " + chunks.size());

        // 2. Generate embeddings for each chunk in a single batch call to avoid rate limiting
        List<List<Double>> embeddings = TextEmbeddingsDemo.generateEmbeddings(chunks, "document");

        // 3. Create a vector store and add each chunk with its pre-computed embedding
        VectorIndex store = new VectorIndex();
        for (int i = 0; i < chunks.size(); i++) {
            store.addVector(embeddings.get(i), Map.of("content", chunks.get(i)));
        }
        System.out.println("Vector store size: " + store.size() + "\n");

        // 4. Some time later, a user asks a question — generate an embedding for it
        String question = "What did the software engineering dept do last year?";
        List<Double> queryEmbedding = TextEmbeddingsDemo.generateEmbedding(question, "query");

        // 5. Search the store with the embedding, find the 2 most relevant chunks
        System.out.println("Query: " + question);
        System.out.println("Top 2 results:\n");
        for (var result : store.search(queryEmbedding, 2)) {
            System.out.printf("Distance: %.4f%n%s%n%n", result.getValue(), result.getKey().get("content"));
        }
    }
}
