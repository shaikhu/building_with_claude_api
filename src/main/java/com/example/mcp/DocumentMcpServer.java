package com.example.mcp;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP server: exposes a document store via tools, resources, and prompt templates.
 *
 */
public class DocumentMcpServer {

    private final Map<String, String> docs = new LinkedHashMap<>();

    public DocumentMcpServer() {
        docs.put("deposition.md",
                "Deposition of Angela Smith, P.E., Senior Structural Engineer. " +
                "She testified that the condenser tower showed signs of corrosion on its lower support beams " +
                "during her inspection on March 15, 2024. Standard maintenance had not been performed for at " +
                "least 18 months prior. Estimated repair cost: $2.4 million over 6 months.");

        docs.put("report.pdf",
                "Structural Assessment Report — 20m Condenser Tower. " +
                "The tower was found to be operating at 73% efficiency due to scaling and minor corrosion. " +
                "Immediate action is recommended on sections B-4 and C-2. Full replacement not required.");

        docs.put("financials.docx",
                "Project Budget Summary. Total approved budget: $3.1 million. " +
                "Spent to date: $820,000 (engineering and site preparation). " +
                "Remaining: $2.28 million. Contingency reserve: 10% ($310,000). " +
                "Current trajectory is on budget and on schedule.");

        docs.put("outlook.pdf",
                "Performance Outlook — 5-Year Projection. Following repairs, the system is expected to " +
                "return to 95% efficiency within 3 months. Annual maintenance costs projected at $45,000. " +
                "ROI breakeven estimated at 2.8 years post-repair.");

        docs.put("plan.md",
                "Implementation Plan. Phase 1: Site assessment and procurement (weeks 1–4). " +
                "Phase 2: Structural reinforcement of sections B-4 and C-2 (weeks 5–12). " +
                "Phase 3: Coating and anti-corrosion treatment (weeks 13–16). " +
                "Phase 4: Commissioning and handover (weeks 17–18).");

        docs.put("spec.txt",
                "Technical Specifications — Condenser Tower Repair. " +
                "Material: Grade 316L stainless steel for replacement sections. " +
                "Coating: Epoxy-based anti-corrosion, min 250 micron DFT. " +
                "Welding standard: AWS D1.1. " +
                "Inspection interval post-repair: every 6 months for first 2 years.");
    }

    // -------------------------------------------------------------------------
    // Tools
    // -------------------------------------------------------------------------

    public List<Tool> getToolDefinitions() {
        Tool readDoc = Tool.builder()
                .name("read_doc")
                .description("Read the full contents of a document by its ID. " +
                        "Call this whenever you need to access a specific document.")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("doc_id", JsonValue.from(Map.of(
                                        "type", "string",
                                        "description", "The document ID to read, e.g. 'deposition.md' or 'report.pdf'"
                                )))
                                .build())
                        .addRequired("doc_id")
                        .build())
                .build();

        Tool editDoc = Tool.builder()
                .name("edit_doc")
                .description("Replace the contents of a document with new text. " +
                        "Read the document first so you can preserve existing information that should not change.")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("doc_id", JsonValue.from(Map.of(
                                        "type", "string",
                                        "description", "The document ID to edit"
                                )))
                                .putAdditionalProperty("content", JsonValue.from(Map.of(
                                        "type", "string",
                                        "description", "The new content to write to the document"
                                )))
                                .build())
                        .addRequired("doc_id")
                        .addRequired("content")
                        .build())
                .build();

        return List.of(readDoc, editDoc);
    }

    public String executeTool(String name, JsonNode input) {
        return switch (name) {
            case "read_doc"  -> readDoc(input.get("doc_id").asText());
            case "edit_doc"  -> editDoc(input.get("doc_id").asText(), input.get("content").asText());
            default          -> "Unknown tool: " + name;
        };
    }

    private String readDoc(String docId) {
        String content = docs.get(docId);
        return content != null ? content : "Document not found: " + docId;
    }

    private String editDoc(String docId, String content) {
        if (!docs.containsKey(docId)) {
            return "Document not found: " + docId;
        }
        docs.put(docId, content);
        return "Successfully updated " + docId;
    }

    // -------------------------------------------------------------------------
    // Resources
    // -------------------------------------------------------------------------

    public List<String> listDocIds() {
        return List.copyOf(docs.keySet());
    }

    public String getDocContent(String docId) {
        String content = docs.get(docId);
        return content != null ? content : "Document not found: " + docId;
    }

    // -------------------------------------------------------------------------
    // Prompts
    // -------------------------------------------------------------------------

    public String getSummarizePrompt(String docId) {
        return """
                Please summarize the following document '%s' in 2–3 concise sentences, \
                capturing the most important facts:

                %s
                """.formatted(docId, getDocContent(docId));
    }

    public String getRewriteMarkdownPrompt(String docId) {
        return """
                Please rewrite the following document '%s' in well-formatted Markdown. \
                Use appropriate headers (##), bullet points, and bold text to improve readability. \
                Do not add information that is not already present:

                %s
                """.formatted(docId, getDocContent(docId));
    }
}
