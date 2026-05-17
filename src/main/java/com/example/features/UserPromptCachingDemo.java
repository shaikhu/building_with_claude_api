package com.example.features;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Usage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class UserPromptCachingDemo {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String longPrompt = Files.readString(
                Paths.get(UserPromptCachingDemo.class.getResource("long_prompt.txt").toURI())
        );

        // The cache breakpoint is placed on the large prompt block.
        // Everything up to and including this block is cached as a single prefix.
        // The follow-up question is a separate block and is NOT cached — it can
        // change freely between requests without invalidating the cached prefix.
        ContentBlockParam cachedPromptBlock = ContentBlockParam.ofText(
                TextBlockParam.builder()
                        .text(longPrompt)
                        .cacheControl(CacheControlEphemeral.builder().build())
                        .build()
        );

        ContentBlockParam question1 = ContentBlockParam.ofText(
                TextBlockParam.builder().text("What global functions are available in the sandbox?").build()
        );

        ContentBlockParam question2 = ContentBlockParam.ofText(
                TextBlockParam.builder().text("What are the rules around document editing?").build()
        );

        // Request 1 — cache is cold, the prompt prefix is written to the cache
        Message first = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessageOfBlockParams(List.of(cachedPromptBlock, question1))
                        .build()
        );
        System.out.println("=== Request 1: " + question1.asText().text() + " ===");
        System.out.println(first.content().getFirst().asText().text());
        printCacheUsage(first.usage());

        // Request 2 — same cached prefix, different question; the prompt is read from cache
        Message second = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_SONNET_4_5)
                        .maxTokens(256)
                        .addUserMessageOfBlockParams(List.of(cachedPromptBlock, question2))
                        .build()
        );
        System.out.println("\n=== Request 2: " + question2.asText().text() + " ===");
        System.out.println(second.content().getFirst().asText().text());
        printCacheUsage(second.usage());
    }

    private static void printCacheUsage(Usage usage) {
        System.out.printf("  input tokens:          %d%n", usage.inputTokens());
        System.out.printf("  cache creation tokens: %d%n", usage.cacheCreationInputTokens().orElse(0L));
        System.out.printf("  cache read tokens:     %d%n", usage.cacheReadInputTokens().orElse(0L));
    }
}
