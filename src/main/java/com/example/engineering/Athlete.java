package com.example.engineering;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Athlete(int height, int weight, String sport, String goal, String restrictions, String activity) {
}
