package com.example.api;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;


public class ChatHistoryDemo {
    public static void main(String[] args) {
        // Reads ANTHROPIC_API_KEY from the environment
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String firstPrompt = "What is quantum computing? Answer in one sentence.";
        Message firstMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessage(firstPrompt)
                        .build()
        );

        // Extract the reply text so we can include it in the next request
        String firstReply = firstMessage.content().getFirst().asText().text();
        System.out.println(firstReply);

        // Replay the full conversation history so the model has context
        String secondPrompt = "Writer another sentence.";
        Message secondMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessage(firstPrompt)
                        .addAssistantMessage(firstReply)
                        .addUserMessage(secondPrompt)
                        .build()
        );

        System.out.println(secondMessage.content().getFirst().asText().text());
    }
}
