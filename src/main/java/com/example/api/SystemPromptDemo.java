package com.example.api;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;


public class SystemPromptDemo {

    public static void main(String[] args) {
        // Reads ANTHROPIC_API_KEY from the environment
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String systemPrompt = """
                You are a patient math tutor. Do not directly answer a student's questions.
                Guide them to a solution step by step.
                """;

        String firstPrompt = "How do I solve 5x + 2 = 3 for x?";
        Message firstMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .system(systemPrompt)
                        .addUserMessage(firstPrompt)
                        .build()
        );

        // Extract the reply text so we can include it in the next request
        String firstReply = firstMessage.content().getFirst().asText().text();
        System.out.println(firstReply);
    }
}
