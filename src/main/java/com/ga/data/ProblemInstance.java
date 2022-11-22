package com.ga.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemInstance(
    String name,
    @JsonProperty("outer_boundary")
    List<Point> outerBoundary,
    List<List<Point>> holes
) {
}
