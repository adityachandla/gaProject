package com.ga.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@EqualsAndHashCode
public class LineSegment {
  private Point start, end;
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private LineSegment next;
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private LineSegment prev;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private LineSegment sibling;

  @EqualsAndHashCode.Exclude
  private int faceReferenceId;

  public LineSegment(Point start, Point end) {
    this.start = start;
    this.end = end;
  }

  public void addReferenceToPoints() {
    start.getSegments().add(this);
    end.getSegments().add(this);
  }

  public void removeReferenceFromPoints() {
    start.getSegments().remove(this);
    end.getSegments().remove(this);
  }

}
