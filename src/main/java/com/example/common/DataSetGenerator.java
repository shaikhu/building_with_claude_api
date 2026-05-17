package com.example.common;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

/**
 * Creates data set (json format) which can be used for evaluating prompts.
 *
 */
public class DataSetGenerator {
    private static final String PROMPT = """
            Generate an verdict dataset for a prompt verdict.
            
            {description}
            
            Generate an array of JSON objects, each storing the different inputs required by the prompt.

            Example output:
            ```json
            {example}
            ```
            
            {additionalCriteria}

            * Please generate 3 objects
            """;

    public static String generate(String description, String example, String additionalCriteria) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String formattedPrompt = PROMPT
                .replace("{description}", description)
                .replace("{example}", example)
                .replace("{additionalCriteria}", additionalCriteria);

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_4_5)
                .maxTokens(256)
                .addUserMessage(formattedPrompt)
                .addAssistantMessage("```json")
                .addStopSequence("```")
                .build();

        return client.messages().create(params).content().getFirst().asText().text();
    }
}