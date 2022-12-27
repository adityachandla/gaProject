package com.ga.data;

public record BoundaryPoint(Point point, boolean isLeftBoundary) {

  public static BoundaryPoint right(Point p) {
    return new BoundaryPoint(p, false);
  }

  public static BoundaryPoint left(Point p) {
    return new BoundaryPoint(p, true);
  }

}
