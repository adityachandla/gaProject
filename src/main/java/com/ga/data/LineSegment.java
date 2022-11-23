package com.ga.data;

import lombok.Data;


@Data
public class LineSegment {
  private Point start, end;
  private LineSegment next;
  private LineSegment prev;
  private LineSegment triangulationSegment; //The segment that may be added during triangulation.

  public LineSegment(Point start, Point end) {
    this.start = start;
    this.end = end;
  }
}
