package com.example.api;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;


public class TemperatureDemo {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String firstPrompt = "Generate a one sentence movie idea";
        Message firstMessage = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessage(firstPrompt)
                        .temperature(1.0) // turn up temperature for tasks which require creativity
                        .build()
        );

        String firstReply = firstMessage.content().getFirst().asText().text();
        System.out.println(firstReply);
    }
}
