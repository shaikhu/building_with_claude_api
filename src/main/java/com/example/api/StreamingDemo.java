package com.example.api;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.RawMessageStreamEvent;

public class StreamingDemo {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5)
                .maxTokens(1024)
                .addUserMessage("Tell me a short story about a robot learning to paint.")
                .build();

        try (StreamResponse<RawMessageStreamEvent> stream = client.messages().createStreaming(params)) {
            stream.stream().forEach(event -> {
                // Each event is a RawMessageStreamEvent union type.
                // We only care about content_block_delta events, which carry text chunks.
                if (event.isContentBlockDelta()) {
                    var delta = event.asContentBlockDelta().delta();
                    // Delta is also a union — could be text, thinking, inputJson, etc.
                    if (delta.isText()) {
                        System.out.print(delta.asText().text());
                        System.out.flush(); // push each chunk to stdout immediately
                    }
                }
            });
        }

        System.out.println(); // newline once the stream is done
    }
}
