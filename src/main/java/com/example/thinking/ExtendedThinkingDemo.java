package com.example.thinking;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

/**
 * To use extended thinking
 *
 * <ol>
 *  <li>a minimum of 1024 budget tokens must be used.</li>
 *  <li> max tokens must be larger than budget tokens (leave room for the response)</li>
 * </ol>
 *
 */
public class ExtendedThinkingDemo {
    public static void main(String... args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(4000)
                        .enabledThinking(1024)
                        .addUserMessage("Is tea or coffee better at breakfast?")
                        .build()
        );

        // With thinking enabled, content blocks are [ThinkingBlock, TextBlock].
        // Print each block so both the reasoning and the final answer are visible.
        for (ContentBlock block : message.content()) {
            if (block.isThinking()) {
                System.out.println("=== Thinking ===");
                System.out.println(block.asThinking().thinking());
            } else if (block.isText()) {
                System.out.println("=== Response ===");
                System.out.println(block.asText().text());
            }
        }
    }
}
