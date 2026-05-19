package com.example.api;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;


public class StructuredDataDemo {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        // Example 1
        String prompt = "Generate a very short event bridge rule as json";

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessage(prompt)
                        .addAssistantMessage("```json")
                        .addStopSequence("```")
                        .build()
        );
        String reply = message.content().getFirst().asText().text();
        System.out.println(reply);

        // Example 2
        prompt = "Generate 3 different sample AWS CLI commands. Each should be very short";

        message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessage(prompt)
                        .addAssistantMessage("Here are all three commands in a single block without any comments:\n```bash")
                        .addStopSequence("```")
                        .build()
        );

        reply = message.content().getFirst().asText().text();
        System.out.println(reply);
    }
}
