package com.example.mcp;

import com.anthropic.models.messages.Tool;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * MCP client: discovers and invokes capabilities exposed by the DocumentMcpServer.
 *
 */
public class McpClient {

    private final DocumentMcpServer server;

    public McpClient(DocumentMcpServer server) {
        this.server = server;
    }

    // -------------------------------------------------------------------------
    // Tools
    // -------------------------------------------------------------------------

    public List<Tool> listTools() {
        return server.getToolDefinitions();
    }

    public String callTool(String name, JsonNode input) {
        return server.executeTool(name, input);
    }

    // -------------------------------------------------------------------------
    // Prompts
    // -------------------------------------------------------------------------

    public List<String> listPrompts() {
        return List.of("summarize", "rewrite_markdown");
    }

    public String getPrompt(String name, String docId) {
        return switch (name) {
            case "summarize"         -> server.getSummarizePrompt(docId);
            case "rewrite_markdown"  -> server.getRewriteMarkdownPrompt(docId);
            default -> throw new IllegalArgumentException("Unknown prompt: " + name);
        };
    }

    // -------------------------------------------------------------------------
    // Resources  (URI scheme: docs://documents[/{doc_id}])
    // -------------------------------------------------------------------------

    public Object readResource(String uri) {
        if (uri.equals("docs://documents")) {
            return server.listDocIds();
        }
        if (uri.startsWith("docs://documents/")) {
            String docId = uri.substring("docs://documents/".length());
            return server.getDocContent(docId);
        }
        throw new IllegalArgumentException("Unknown resource URI: " + uri);
    }
}
