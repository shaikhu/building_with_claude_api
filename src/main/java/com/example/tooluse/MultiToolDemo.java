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

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates registering multiple different tools and dispatching each tool call by name.
 */
public class MultiToolDemo {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();
        ObjectMapper mapper = new ObjectMapper();

        String prompt = "What is the current time formatted as HH:mm:ss, and also make the number 42 cooler";

        // Turn 1: advertise all available tools — the model picks whichever it needs
        Message firstMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addTool(CurrentDateTimeTool.DEFINITION)
                        .addTool(CoolNumberTool.DEFINITION)
                        .addUserMessage(prompt)
                        .build()
        );

        List<ToolUseBlock> toolUseBlocks = firstMessage.content().stream()
                .filter(ContentBlock::isToolUse)
                .map(ContentBlock::asToolUse)
                .toList();

        if (toolUseBlocks.isEmpty()) {
            throw new IllegalStateException("Model did not call any tools");
        }

        // Execute each tool call and collect results
        List<ContentBlockParam> toolResults = new ArrayList<>();
        for (ToolUseBlock toolUseBlock : toolUseBlocks) {
            JsonNode input = mapper.convertValue(toolUseBlock._input(), JsonNode.class);

            String result = switch (toolUseBlock.name()) {
                case "get_current_date_time" -> {
                    String format = input.get("format").asText();
                    yield CurrentDateTimeTool.getCurrentDateTime(format);
                }
                case "make_number_cool" -> {
                    int num = input.get("num").asInt();
                    yield Integer.toString(CoolNumberTool.makeCool(num));
                }
                default -> throw new IllegalStateException("Unknown tool: " + toolUseBlock.name());
            };

            System.out.println("Tool called: " + toolUseBlock.name() + " -> " + result);

            toolResults.add(ContentBlockParam.ofToolResult(
                    ToolResultBlockParam.builder()
                            .toolUseId(toolUseBlock.id())
                            .content(result)
                            .build()
            ));
        }

        // Turn 2: return all results, replaying the full conversation history
        Message secondMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addTool(CurrentDateTimeTool.DEFINITION)
                        .addTool(CoolNumberTool.DEFINITION)
                        .addUserMessage(prompt)
                        .addMessage(firstMessage)
                        .addUserMessageOfBlockParams(toolResults)
                        .build()
        );

        System.out.println(secondMessage.content().getFirst().asText().text());
    }
}
