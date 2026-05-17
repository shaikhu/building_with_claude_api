package com.example.common;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Ask Claude to evaluate AI generated solutions.
 */
public class LlmJudge {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String prompt(String role, String input, String solution, String criteria) {
        return """
               You are a %s. Your task is to evaluate the following AI-generated output.

               Input:
               <input>
               %s
               </input>

               Output to evaluate:
               <solution>
               %s
               </solution>

               Scoring Criteria:
               %s

               Output Format
               Provide your verdict as a structured JSON object with the following fields:
               - "strengths": An array of 1-3 key strengths
               - "weaknesses": An array of 1-3 key areas for improvement
               - "reasoning": A concise explanation of your overall assessment
               - "score": A number between 1-10

               Respond with JSON only. Keep your response concise and direct.
               {
                   "strengths": string[],
                   "weaknesses": string[],
                   "reasoning": string,
                   "score": number
               }
               """.formatted(role, input, solution, criteria);
    }

    public static Verdict judge(AnthropicClient client, String role, String criteria, String input, String solution) {
        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_HAIKU_4_5)
                        .maxTokens(1024)
                        .addUserMessage(prompt(role, input, solution, criteria))
                        .addAssistantMessage("```json")
                        .addStopSequence("```")
                        .build()
        );

        try {
            String json = message.content().getFirst().asText().text();
            return MAPPER.readValue(json, Verdict.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
