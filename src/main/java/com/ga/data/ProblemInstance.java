package com.ga.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @param name          Input name
 * @param outerBoundary Points sorted in counter-clockwise order
 * @param holes         List of Points where points in each list are sorted clockwise
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemInstance(
    String name,
    @JsonProperty("outer_boundary")
    List<Point> outerBoundary,
    List<List<Point>> holes
) {
}
