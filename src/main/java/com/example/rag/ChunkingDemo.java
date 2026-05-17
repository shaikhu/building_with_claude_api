package com.example.rag;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Demonstrates different strategies for chunking text from a document.
 * Part of the Retrieval Augmented Generation (RAG) flow.
 */
public class ChunkingDemo {
    /**
     * Chunk by a fixed number of characters.
     */
    public static List<String> chunkByChar(String text) {
        return chunkByChar(text, 150, 20);
    }


    /**
     * Chunk by a fixed number of characters using supplied chunkSize, and chunkOverlap.
     */
    public static List<String> chunkByChar(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();

        int startIdx = 0;

        while (startIdx < text.length()) {
            int endIdx = Math.min(startIdx + chunkSize, text.length());

            String chunkText = text.substring(startIdx, endIdx);
            chunks.add(chunkText);

            startIdx = (endIdx < text.length())
                    ? endIdx - chunkOverlap
                    : text.length();
        }

        return chunks;
    }


    /**
     * Chunk by sentence.
     */
    public static List<String> chunkBySentence(String text, int maxSentencesPerChunk, int overlapSentences) {
        String[] sentences = text.split("(?<=[.!?])\\s+");

        List<String> chunks = new ArrayList<>();

        int startIdx = 0;

        while (startIdx < sentences.length) {
            int endIdx = Math.min(
                    startIdx + maxSentencesPerChunk,
                    sentences.length
            );

            String chunk = String.join(
                    " ",
                    Arrays.copyOfRange(sentences, startIdx, endIdx)
            );

            chunks.add(chunk);

            startIdx += maxSentencesPerChunk - overlapSentences;

            if (startIdx < 0) {
                startIdx = 0;
            }
        }

        return chunks;
    }


    /**
     * Chunk by markdown section headers (##).
     */
    public static List<String> chunkBySection(String documentText) {
        Pattern pattern = Pattern.compile("\\n## ");
        return Arrays.asList(pattern.split(documentText));
    }


    public static void main(String... args) throws Exception {
        String text = Files.readString(Paths.get(ChunkingDemo.class.getResource("report.md").toURI()));

        List<String> chunks = chunkBySection(text);

        for (String chunk : chunks) {
            System.out.println(chunk);
            System.out.println("\n----\n");
        }
    }
}
