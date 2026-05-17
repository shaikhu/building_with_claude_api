package com.example.features;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Usage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SystemPromptCachingDemo {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        // A large system prompt is required — cache only activates above 1024 tokens (Sonnet).
        // The cache breakpoint is set on the TextBlockParam, not on the request itself.
        String systemPromptText = Files.readString(
                Paths.get(SystemPromptCachingDemo.class.getResource("earth_article.txt").toURI())
        );

        TextBlockParam systemBlock = TextBlockParam.builder()
                .text(systemPromptText)
                .cacheControl(CacheControlEphemeral.builder().build())
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5)
                .maxTokens(256)
                .systemOfTextBlockParams(List.of(systemBlock))
                .addUserMessage("What is Earth's average surface temperature?")
                .build();

        // Request 1 — cache is cold, so tokens are written to the cache
        Message first = client.messages().create(params);
        System.out.println("=== Request 1 ===");
        System.out.println(first.content().getFirst().asText().text());
        printCacheUsage(first.usage());

        // Request 2 — identical prefix, so the cache is read instead of reprocessed
        Message second = client.messages().create(params);
        System.out.println("\n=== Request 2 ===");
        System.out.println(second.content().getFirst().asText().text());
        printCacheUsage(second.usage());
    }

    private static void printCacheUsage(Usage usage) {
        System.out.printf("  input tokens:          %d%n", usage.inputTokens());
        System.out.printf("  cache creation tokens: %d%n", usage.cacheCreationInputTokens().orElse(0L));
        System.out.printf("  cache read tokens:     %d%n", usage.cacheReadInputTokens().orElse(0L));
    }
}
