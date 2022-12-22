package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

public class GeometryUtil {
  public enum OrientationResult {
    RIGHT, LEFT, COLLINEAR
  }

  public enum PointType {
    START, SPLIT, END, MERGE, REGULAR
  }

  private enum DirectionChange {
    NONE, UP_TO_DOWN, DOWN_TO_UP
  }

  /**
   * Tests if point c is left, right or collinear with line from a to b
   *
   * @param p Point
   * @param q Point
   * @param r Point
   * @return OrientationResult
   */
  public static OrientationResult orientationTest(Point p, Point q, Point r) {
    int res = (p.getX() * (q.getY() - r.getY())) + (q.getX() * (r.getY() - p.getY())) +
        (r.getX() * (p.getY() - q.getY()));
    if (res == 0) {
      return OrientationResult.COLLINEAR;
    } else if (res > 0) {
      return OrientationResult.LEFT;
    }
    return OrientationResult.RIGHT;
  }

  public static PointType getPointType(Point p) {
    var prevNext = getPrevNext(p);
    var prev = prevNext.prev();
    var next = prevNext.next();
    var directionChange = getDirectionChangeY(prev, next);
    if (directionChange == DirectionChange.NONE) {
      return PointType.REGULAR;
    }
    var orientation = orientationTest(prev.getStart(), prev.getEnd(), next.getEnd());
    if (orientation == OrientationResult.LEFT) {
      //Start or end: check if direction change from top to bottom or bottom to top
      return directionChange == DirectionChange.UP_TO_DOWN ? PointType.START : PointType.END;
    }
    if (orientation == OrientationResult.RIGHT) {
      //Start or end: check if direction change from top to bottom or bottom to top
      return directionChange == DirectionChange.UP_TO_DOWN ? PointType.SPLIT : PointType.MERGE;
    }
    //If there is direction change then the three points can not be COLLINEAR
    throw new IllegalStateException("Points can not be COLLINEAR with direction change");
  }

  private static DirectionChange getDirectionChangeY(LineSegment prev, LineSegment next) {
    var precision = Precision.doubleEquivalenceOfEpsilon(1e-10);
    var prevSegment = Lines.fromPoints(toVector2D(prev.getStart()), toVector2D(prev.getEnd()), precision);
    var nextSegment = Lines.fromPoints(toVector2D(next.getStart()), toVector2D(next.getEnd()), precision);
    var prevDirection = prevSegment.getDirection();
    var nextDirection = nextSegment.getDirection();
    if (prevDirection.getY() > 0 && nextDirection.getY() < 0) {
      return DirectionChange.UP_TO_DOWN;
    } else if (prevDirection.getY() < 0 && nextDirection.getY() > 0) {
      return DirectionChange.DOWN_TO_UP;
    }
    return DirectionChange.NONE;
  }

  private static Vector2D toVector2D(Point p) {
    return Vector2D.of(p.getX(), p.getY());
  }

  public static PrevNext getPrevNext(Point point) {
    var prev = point.getSegments().get(0);
    var next = point.getSegments().get(1);
    if (prev.getNext() != next) {
      var temp = next;
      next = prev;
      prev = temp;
    }
    assert prev.getNext() == next;
    return new PrevNext(prev, next);
  }

  public record PrevNext(LineSegment prev, LineSegment next) {
  }
}
