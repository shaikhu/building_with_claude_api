package com.example.tooluse;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Demonstrates single tool usage.
 *
 * - User sends a prompt with a list of available tools
 * - The model will decide whether to call the tool
 * - Code executes the tool locally and sends the result back
 * - The model uses the result to respond
 */
public class BasicToolDemo {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String prompt = "What is the exact time, formatted as HH:MM:SS?";

        // Turn 1: ask the model, which should respond by calling the tool
        Message firstMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addTool(CurrentDateTimeTool.DEFINITION)
                        .addUserMessage(prompt)
                        .build()
        );

        // Find the tool-use block in the response
        ToolUseBlock toolUseBlock = firstMessage.content().stream()
                .filter(ContentBlock::isToolUse)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Model did not call a tool"))
                .asToolUse();

        // Execute the tool locally
        JsonNode input = new ObjectMapper().convertValue(toolUseBlock._input(), JsonNode.class);
        String format = input.get("format").asText();
        String toolResult = CurrentDateTimeTool.getCurrentDateTime(format);
        System.out.println("Tool called: " + toolUseBlock.name() + "(\"" + format + "\") -> " + toolResult);

        // Turn 2: return the tool result, replaying the full conversation history
        ToolResultBlockParam resultParam = ToolResultBlockParam.builder()
                .toolUseId(toolUseBlock.id()) // links back to the specific tool call
                .content(toolResult)
                .build();

        Message secondMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addTool(CurrentDateTimeTool.DEFINITION)
                        .addUserMessage(prompt)
                        .addMessage(firstMessage)
                        .addUserMessageOfBlockParams(List.of(ContentBlockParam.ofToolResult(resultParam)))
                        .build()
        );

        System.out.println(secondMessage.content().getFirst().asText().text());
    }
}
