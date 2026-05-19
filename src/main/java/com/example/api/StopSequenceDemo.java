package com.example.api;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;


public class StopSequenceDemo {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String prompt = "Count from 1 to 10?";

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessage(prompt)
                        .addStopSequence("5")
                        .build()
        );

        String reply = message.content().getFirst().asText().text();
        System.out.println(reply);
    }
}
