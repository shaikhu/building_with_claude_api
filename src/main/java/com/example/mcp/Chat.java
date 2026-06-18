package com.example.mcp;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agentic chat loop that connects Claude to MCP tools.
 *
 */
public class Chat {

    private final AnthropicClient client;
    private final McpClient mcpClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MessageCreateParams.Builder builder;

    public Chat(AnthropicClient client, McpClient mcpClient) {
        this.client = client;
        this.mcpClient = mcpClient;

        builder = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5)
                .maxTokens(8000);

        mcpClient.listTools().forEach(builder::addTool);
    }

    public String run(String query) {
        builder.addUserMessage(query);

        while (true) {
            Message response = client.messages().create(builder.build());

            List<ToolUseBlock> toolUseBlocks = response.content().stream()
                    .filter(ContentBlock::isToolUse)
                    .map(ContentBlock::asToolUse)
                    .toList();

            if (toolUseBlocks.isEmpty()) {
                return textFromMessage(response);
            }

            // Print any explanatory text Claude included before the tool calls
            String interim = textFromMessage(response);
            if (!interim.isBlank()) {
                System.out.println(interim);
            }

            // Execute each tool the model requested
            List<ContentBlockParam> toolResults = new ArrayList<>();
            for (ToolUseBlock toolUse : toolUseBlocks) {
                JsonNode input = mapper.convertValue(toolUse._input(), JsonNode.class);
                String result = mcpClient.callTool(toolUse.name(), input);
                System.out.println("  [tool] " + toolUse.name() + "(" + input + ") → " + result);

                toolResults.add(ContentBlockParam.ofToolResult(
                        ToolResultBlockParam.builder()
                                .toolUseId(toolUse.id())
                                .content(result)
                                .build()
                ));
            }

            // Append the assistant turn and the tool results to conversation history
            builder.addMessage(response);
            builder.addUserMessageOfBlockParams(toolResults);
        }
    }

    private String textFromMessage(Message message) {
        return message.content().stream()
                .filter(ContentBlock::isText)
                .map(b -> b.asText().text())
                .collect(Collectors.joining("\n"));
    }
}
