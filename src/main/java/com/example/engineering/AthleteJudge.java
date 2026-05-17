package com.example.engineering;

public interface AthleteJudge {

    String ROLE = "expert nutritionist who works with world class Olympic athletes";

    String CRITERIA = """
            When scoring, consider whether the plan covers:
            - Daily caloric total
            - Macronutrient breakdown (protein, fat, carbs)
            - Meals with exact foods, portions, and timings
            - How well the plan follows the specified restrictions

            IMPORTANT SCORING INSTRUCTIONS:
            * Grade ONLY on the listed criteria.
            * If all criteria are met, give a 10.
            * ANY violation of a mandatory requirement MUST result in a score of 3 or lower.
            * Use the full 1-10 scale.
            """;

}
