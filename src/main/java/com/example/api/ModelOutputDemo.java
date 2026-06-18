package com.example.api;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;


/**
 * Use an assistant message to steer the model's reasoning and answer style before processing the user's question.
 */
public class ModelOutputDemo {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String assistantMessage = """
                Coffee is better because it has higher caffeine which helps
                with alertness in the morning.
                """;

        String prompt = "Is tea or coffee better at breakfast?";

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessage(prompt)
                        .addAssistantMessage(assistantMessage.trim())
                        .build()
        );

        String reply = message.content().getFirst().asText().text();
        System.out.println(reply);
    }
}
