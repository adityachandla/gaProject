package com.ga.data;

import lombok.Data;


@Data
public class LineSegment {
  private Point start, end;
  private LineSegment next;
  private LineSegment prev;

  public LineSegment(Point start, Point end) {
    this.start = start;
    this.end = end;
  }

  public void addReferenceToPoints() {
    start.getSegments().add(this);
    end.getSegments().add(this);
  }

  public void deleteReferenceFromPoints() {
    start.getSegments().remove(this);
    end.getSegments().remove(this);
  }
}
