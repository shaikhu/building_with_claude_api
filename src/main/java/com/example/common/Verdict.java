package com.example.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Verdict(int score, String reasoning, List<String> strengths, List<String> weaknesses) {
}
