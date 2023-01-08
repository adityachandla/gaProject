package com.ga.data;

import java.util.List;

public record ProblemOutput(
    String type,
    String instance,
    List<List<Point>> polygons
) {}
