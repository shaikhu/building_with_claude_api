package com.example.engineering;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.example.common.DataSetGenerator;
import com.example.common.LlmJudge;
import com.example.common.Verdict;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.example.engineering.AthleteJudge.CRITERIA;
import static com.example.engineering.AthleteJudge.ROLE;

/**
 * Demonstration of prompt engineering.
 *
 * <ul>
 *     <li>Generate data set of Athletes workout goals and diet restrictions.</li>
 *     <li>Ask Claude to generate meal plans.</li>
 *     <li>Use prompt engineering techniques to improve prompt e.g. one-shot</li>
 *     <li>Evaluate the solutions on a scale of 1-10.</li>
 *     <li>Generate results.html to display summary and stats.</li>
 * </ul>
 */
public class PromptEngineering {

    public static void main(String... args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String jsonDataSet = DataSetGenerator.generate(AthleteDataSet.DESCRIPTION, AthleteDataSet.EXAMPLE, AthleteDataSet.ADDITIONAL_CRITERIA);
        String idealMealPlan = Files.readString(Paths.get(PromptEngineering.class.getResource("ideal_meal_plan_example.txt").toURI()));

        List<Athlete> athletes = new ObjectMapper()
                .readerForListOf(Athlete.class)
                .readValue(jsonDataSet);

        List<Result> results = new ArrayList<>();
        for (Athlete athlete : athletes) {

            String prompt = """
                    Generate a one-day meal plan for an athlete that meets their dietary restrictions.
                    
                    - Height: %d
                    - Weight: %d
                    - Sport: %s
                    - Goal: %s
                    - Restrictions: %s

                    Guidelines:
                    1. Include accurate daily calorie amount
                    2. Show protein, fat, and carb amounts
                    3. Specify when to eat each meal
                    4. Use only food that fit restrictions
                    5. List all portion sizes in grams
                    6. Keep budget-friendly if mentioned
                    
                    
                    Here is an example representing a sample input with an ideal output/response.
                    
                    <sample_input>
                        height:168
                        weight:58
                        goal:Maintain lean physique and endurance
                        sport:runner
                        restrictions:Vegetarian
                    </sample_input>
                    
                    <ideal_output>
                    %s
                    </ideal_output>
                    
                    This meal plan comprehensively addresses all four scoring criteria: daily caloric total is virtually exact (2,390/2,400), macronutrient breakdown matches targets precisely (16%% protein, 55%% carbs, 25%% fat), meals include specific foods with exact portions and clear timings across six eating occasions, and vegetarian restrictions are fully honored. 
                    The plan demonstrates strong nutritional knowledge with appropriate pre/post-run fueling and lean muscle maintenance focus.
                    """.formatted(athlete.height(), athlete.weight(),  athlete.sport(), athlete.goal(), athlete.restrictions(), idealMealPlan);

            Message message = client.messages().create(
                    MessageCreateParams.builder()
                            .model(Model.CLAUDE_HAIKU_4_5)
                            .maxTokens(1024)
                            .system("do not ask follow up questions")
                            .addUserMessage(prompt)
                            .build()
            );

            String input = """
                    height:%d
                    weight:%d
                    goal:%s
                    restrictions:%s
                    """.formatted(athlete.height(), athlete.weight(), athlete.goal(), athlete.restrictions());

            String solution = message.content().getFirst().asText().text();
            Verdict verdict = LlmJudge.judge(client, ROLE, CRITERIA, input, solution);
            results.add(new Result(solution, athlete, verdict));
        }

        HtmlReport report = new HtmlReport();
        report.create(results);
    }
}
