package com.example.evaluation;


/**
 * Used in prompt for generating AWS technical data sets.
 *
 * The data set will be used to evaluate Claud prompts for evaluating AWS related tasks.
 */
public interface AwsDataSet {

    String DESCRIPTION = "The dataset will be used to evaluate prompts that generate Python, JSON, or Regex specifically for AWS-related tasks";

    String EXAMPLE = """
            {
                "task": "Description of task",
                "format": "json" or "python" or "regex"
            }
            """;

    String ADDITIONAL_CRITERIA = """
            * Focus on tasks that can be solved by writing a single Python function, a single JSON object, or a regular expression.
            * Focus on tasks that do not require writing much code
            """;
}
