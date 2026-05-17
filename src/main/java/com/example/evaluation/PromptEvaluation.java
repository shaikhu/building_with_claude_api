package com.example.evaluation;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.example.common.DataSetGenerator;
import com.example.common.Verdict;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static com.example.common.LlmJudge.judge;
import static com.example.evaluation.AwsDataSet.ADDITIONAL_CRITERIA;
import static com.example.evaluation.AwsDataSet.DESCRIPTION;
import static com.example.evaluation.AwsDataSet.EXAMPLE;
import static com.example.evaluation.AwsJudge.CRITERIA;
import static com.example.evaluation.AwsJudge.ROLE;

/**
 * Demonstration of prompt evaluation.
 *
 * <ul>
 *     <li>Generate data set of AWS tasks to solve.</li>
 *     <li>Ask Claude to solve the AWS tasks.</li>
 *     <li>Evaluate the solutions on a scale of 1-10.</li>
 * </ul>
 */
public class PromptEvaluation {
    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String jsonDataSet = DataSetGenerator.generate(DESCRIPTION, EXAMPLE, ADDITIONAL_CRITERIA);

        List<AwsTask> tasks = new ObjectMapper()
                .readerForListOf(AwsTask.class)
                .readValue(jsonDataSet);

        for (AwsTask awsTask : tasks) {
            String prompt = """
                    Please solve the following task:
                    %s

                    * Respond only with Python, JSON, or a plain Regex
                    * Do not add any comments or commentary or explanation
                    """.formatted(awsTask.task());

            Message message = client.messages().create(
                    MessageCreateParams.builder()
                            .model(Model.CLAUDE_HAIKU_4_5)
                            .maxTokens(1024)
                            .addUserMessage(prompt)
                            .addAssistantMessage("```%s".formatted(awsTask.format()))
                            .addStopSequence("```")
                            .build()
            );

            String solution = message.content().getFirst().asText().text();
            Verdict verdict = judge(client, ROLE, CRITERIA, awsTask.task(), solution);
            int syntaxScore = switch (awsTask.format()) {
                case "json"   -> SyntaxValidator.validateJson(solution);
                case "python" -> SyntaxValidator.validatePython(solution);
                case "regex"  -> SyntaxValidator.validateRegex(solution);
                default       -> 0;
            };

            System.out.printf("Task : %s%n",awsTask.task());
            System.out.printf("Solution : %s%n", solution);
            System.out.printf("Strengths : %s%n", verdict.strengths());
            System.out.printf("Weaknesses : %s%n", verdict.weaknesses());
            System.out.printf("Score : %d%n", (verdict.score() + syntaxScore) / 2);
            System.out.printf("Reasoning : %s%n", verdict.reasoning());
            System.out.println();
        }
    }
}
