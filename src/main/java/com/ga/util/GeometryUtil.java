package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

@Slf4j
public class GeometryUtil {
  private static final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-20);

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
    long res = (p.getX() * (q.getY() - r.getY())) + (q.getX() * (r.getY() - p.getY())) +
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
    DirectionChange directionChange = getDirectionChangeY(prev, next);
    if (directionChange == DirectionChange.NONE) {
      return PointType.REGULAR;
    }
    var orientation = orientationTest(prev.getStart(), prev.getEnd(), next.getEnd());
    if (orientation == OrientationResult.LEFT) {
      //Start or end: check if direction change from top to bottom or bottom to top
      return directionChange == DirectionChange.UP_TO_DOWN ? PointType.START : PointType.END;
    }
    if (orientation == OrientationResult.RIGHT) {
      //Split or merge: check if direction change from top to bottom or bottom to top
      return directionChange == DirectionChange.UP_TO_DOWN ? PointType.SPLIT : PointType.MERGE;
    }
    //If there is direction change then the three points can not be COLLINEAR
    throw new IllegalStateException("Points can not be COLLINEAR with direction change");
  }

  private static DirectionChange getDirectionChangeY(LineSegment prev, LineSegment next) {

    var prevSegment = Lines.fromPoints(toVector2D(prev.getStart()), toVector2D(prev.getEnd()), precision);
    var nextSegment = Lines.fromPoints(toVector2D(next.getStart()), toVector2D(next.getEnd()), precision);

    if (goingUp(prevSegment) && goingDown(nextSegment)) {
      return DirectionChange.UP_TO_DOWN;
    } else if (goingDown(prevSegment) && goingUp(nextSegment)) {
      return DirectionChange.DOWN_TO_UP;
    } else if (goingDown(prevSegment) && goingDown(nextSegment)) {
      return DirectionChange.NONE;
    } else if (goingUp(prevSegment) && goingUp(nextSegment)) {
      return DirectionChange.NONE;
    }
    //going right and going left is also important here
    else if (straight(prevSegment) && goingUp(nextSegment)) {
      return goingLeft(prevSegment) ? DirectionChange.NONE : DirectionChange.DOWN_TO_UP;
    } else if (straight(prevSegment) && goingDown(nextSegment)) {
      return goingLeft(prevSegment) ? DirectionChange.UP_TO_DOWN : DirectionChange.NONE;
    } else if (goingUp(prevSegment) && straight(nextSegment)) {
      return goingLeft(nextSegment) ? DirectionChange.NONE: DirectionChange.UP_TO_DOWN;
    } else if (goingDown(prevSegment) && straight(nextSegment)) {
      return goingLeft(nextSegment) ? DirectionChange.DOWN_TO_UP : DirectionChange.NONE;
    }
    throw new IllegalStateException("Invalid Direction change");
  }

  private static boolean goingLeft(Line line) {
    return precision.lt(line.getDirection().getX(), 0);
  }

  private static boolean goingUp(Line line) {
    return precision.gt(line.getDirection().getY(), 0);
  }

  private static boolean goingDown(Line line) {
    return precision.lt(line.getDirection().getY(), 0);
  }

  private static boolean straight(Line line) {
    return precision.eq(line.getDirection().getY(), 0);
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
