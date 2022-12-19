package com.ga.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * points: List of all points of the holes and polygon
 */
@Data
@AllArgsConstructor
public class Polygon {
  private List<Point> points;
  private LineSegment startingSegment;
  private List<LineSegment> holes;
}
