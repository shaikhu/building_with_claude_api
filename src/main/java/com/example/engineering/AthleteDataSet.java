package com.example.engineering;

/**
 * Used in prompt for generating athlete data sets.
 *
 * The data set will be used to evaluate Claud prompts for evaluating meal plans for high performing athletes.
 */
public interface AthleteDataSet {

    String DESCRIPTION = "The dataset will be used to evaluate prompts that generate a concise 1 day meal plan fora single athlete";

    String EXAMPLE = """
                {
                  "height": "Athlete's height in cm",
                  "weight": "Athlete's weight in kg",
                  "goal"  : "Goal of the athlete",
                  "sport" : "The sport the athlete pursues",
                  "activity" : "The activity level (times and days per week) of the athlete",
                  "restrictions": "Dietary restrictions of the athlete"
                }
                """;

    String ADDITIONAL_CRITERIA = """
                * Do not create additional input attributes. Only use the input provides in the example above.
                * Focus on varying goal of each athlete e.g. weight lifter gaining muscle, runner keeping lean etc
                * Focus on varying dietary restrictions e.g. vegetarian, gluten free etc
                """;
}
