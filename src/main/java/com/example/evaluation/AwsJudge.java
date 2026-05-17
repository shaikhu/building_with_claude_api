package com.example.evaluation;

public interface AwsJudge {

    String ROLE = "expert AWS code reviewer";

    String CRITERIA = """
            - Correctness: Does it solve the task?
            - Clarity: Is it readable and well-structured?
            - Best practices: Does it follow AWS/language conventions?
            """;
}
