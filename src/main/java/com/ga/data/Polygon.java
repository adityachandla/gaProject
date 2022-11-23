package com.ga.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Polygon {
  private List<Point> points;
  private LineSegment startingSegment;
  private List<LineSegment> holes; //Contains the starting line segment of the hole
}
