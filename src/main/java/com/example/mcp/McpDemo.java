package com.example.mcp;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

/**
 * Demonstrates all three MCP primitives: tools, resources, and prompts.
 *
 * Scenario 1 — Resource : list available documents (no Claude call needed)
 * Scenario 2 — Tool use : Claude reads a document to answer a question
 * Scenario 3 — Prompt   : use a server-defined prompt template (summarize)
 * Scenario 4 — Tool use : Claude reads then edits a document
 */
public class McpDemo {

    public static void main(String... args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();
        DocumentMcpServer server = new DocumentMcpServer();
        McpClient mcpClient = new McpClient(server);

        // ── Scenario 1: Resource ─────────────────────────────────────────────
        System.out.println("=== Scenario 1: MCP Resource — List Documents ===");
        System.out.println("Available document IDs:");
        @SuppressWarnings("unchecked")
        var docIds = (java.util.List<String>) mcpClient.readResource("docs://documents");
        docIds.forEach(id -> System.out.println("  - " + id));

        // ── Scenario 2: Tool use — Claude reads a document ───────────────────
        System.out.println("\n=== Scenario 2: MCP Tool — Read Document ===");
        System.out.println("Query: What are the key financial figures for this project?");
        Chat readChat = new Chat(client, mcpClient);
        String readResponse = readChat.run(
                "What are the key financial figures for this project? " +
                "Use the available tools to look up the relevant document."
        );
        System.out.println("Response: " + readResponse);

        // ── Scenario 3: Prompt template — summarize ──────────────────────────
        System.out.println("\n=== Scenario 3: MCP Prompt — Summarize Document ===");
        System.out.println("Running /summarize on deposition.md");
        String summarizePrompt = mcpClient.getPrompt("summarize", "deposition.md");
        Chat promptChat = new Chat(client, mcpClient);
        String summaryResponse = promptChat.run(summarizePrompt);
        System.out.println("Response: " + summaryResponse);

        // ── Scenario 4: Tool use — Claude reads then edits a document ────────
        System.out.println("\n=== Scenario 4: MCP Tool — Edit Document ===");
        System.out.println("Query: Update plan.md to include a testing phase");
        Chat editChat = new Chat(client, mcpClient);
        String editResponse = editChat.run(
                "Please update plan.md to include a testing and QA phase after the commissioning phase. " +
                "Read the current content first, then write the complete updated document."
        );
        System.out.println("Response: " + editResponse);
        System.out.println("\nUpdated plan.md content:");
        System.out.println(mcpClient.readResource("docs://documents/plan.md"));
    }
}
