package com.ga.util;

import com.ga.data.Point;

public class GeometryUtil {
  public enum OrientationResult {
    RIGHT, LEFT, COLLINEAR
  }

  /**
   * Tests if point c is left, right or collinear with line from a to b
   * @param p Point
   * @param q Point
   * @param r Point
   * @return OrientationResult
   */
  public static OrientationResult orientationTest(Point p, Point q, Point r) {
    int res = (p.getX()*(q.getY()-r.getY())) + (q.getX()*(r.getY()-p.getY())) +
        (r.getX()*(p.getY()-q.getY()));
    if (res == 0) {
      return OrientationResult.COLLINEAR;
    } else if (res > 0) {
      return OrientationResult.LEFT;
    }
    return OrientationResult.RIGHT;
  }
}
