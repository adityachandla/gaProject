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

  private enum DirectionChangeY {
    NONE, UP_TO_DOWN, DOWN_TO_UP
  }

  public enum DirectionX {
    LEFT, RIGHT, LEFT_TO_RIGHT, RIGHT_TO_LEFT
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
    DirectionChangeY directionChangeY = getDirectionChangeY(prev, next);
    if (directionChangeY == DirectionChangeY.NONE) {
      return PointType.REGULAR;
    }
    var orientation = orientationTest(prev.getStart(), prev.getEnd(), next.getEnd());
    if (orientation == OrientationResult.LEFT) {
      //Start or end: check if direction change from top to bottom or bottom to top
      return directionChangeY == DirectionChangeY.UP_TO_DOWN ? PointType.START : PointType.END;
    }
    if (orientation == OrientationResult.RIGHT) {
      //Split or merge: check if direction change from top to bottom or bottom to top
      return directionChangeY == DirectionChangeY.UP_TO_DOWN ? PointType.SPLIT : PointType.MERGE;
    }
    //If there is direction change then the three points can not be COLLINEAR
    throw new IllegalStateException("Points can not be COLLINEAR with direction change");
  }

  private static DirectionChangeY getDirectionChangeY(LineSegment prev, LineSegment next) {
    var prevSegment = Lines.fromPoints(toVector2D(prev.getStart()), toVector2D(prev.getEnd()), precision);
    var nextSegment = Lines.fromPoints(toVector2D(next.getStart()), toVector2D(next.getEnd()), precision);

    if (goingUp(prevSegment) && goingDown(nextSegment)) {
      return DirectionChangeY.UP_TO_DOWN;
    } else if (goingDown(prevSegment) && goingUp(nextSegment)) {
      return DirectionChangeY.DOWN_TO_UP;
    } else if (goingDown(prevSegment) && goingDown(nextSegment)) {
      return DirectionChangeY.NONE;
    } else if (goingUp(prevSegment) && goingUp(nextSegment)) {
      return DirectionChangeY.NONE;
    }
    //going right and going left is also important here
    else if (straightY(prevSegment) && goingUp(nextSegment)) {
      return goingLeft(prevSegment) ? DirectionChangeY.NONE : DirectionChangeY.DOWN_TO_UP;
    } else if (straightY(prevSegment) && goingDown(nextSegment)) {
      return goingLeft(prevSegment) ? DirectionChangeY.UP_TO_DOWN : DirectionChangeY.NONE;
    } else if (goingUp(prevSegment) && straightY(nextSegment)) {
      return goingLeft(nextSegment) ? DirectionChangeY.NONE : DirectionChangeY.UP_TO_DOWN;
    } else if (goingDown(prevSegment) && straightY(nextSegment)) {
      return goingLeft(nextSegment) ? DirectionChangeY.DOWN_TO_UP : DirectionChangeY.NONE;
    }
    throw new IllegalStateException("Invalid Direction change");
  }

  public static DirectionX getDirectionChangeX(LineSegment prev, LineSegment next) {
    var prevSegment = Lines.fromPoints(toVector2D(prev.getStart()), toVector2D(prev.getEnd()), precision);
    var nextSegment = Lines.fromPoints(toVector2D(next.getStart()), toVector2D(next.getEnd()), precision);

    if (goingRight(prevSegment) && goingRight(nextSegment)) {
      return DirectionX.RIGHT;
    } else if (goingLeft(prevSegment) && goingLeft(nextSegment)) {
      return DirectionX.LEFT;
    } else if (goingLeft(prevSegment) && goingRight(nextSegment)) {
      return DirectionX.LEFT_TO_RIGHT;
    } else if (goingRight(prevSegment) && goingLeft(nextSegment)) {
      return DirectionX.RIGHT_TO_LEFT;
    } else if (straightX(prevSegment) && goingRight(nextSegment)) {
      return goingUp(prevSegment) ? DirectionX.RIGHT : DirectionX.LEFT_TO_RIGHT;
    } else if (straightX(prevSegment) && goingLeft(nextSegment)) {
      return goingUp(prevSegment) ? DirectionX.RIGHT_TO_LEFT : DirectionX.LEFT;
    } else if (goingLeft(prevSegment) && straightX(nextSegment)) {
      return goingUp(nextSegment) ? DirectionX.LEFT_TO_RIGHT : DirectionX.LEFT;
    } else if (goingRight(prevSegment) && straightX(nextSegment)) {
      return goingUp(nextSegment) ? DirectionX.RIGHT : DirectionX.RIGHT_TO_LEFT;
    }
    throw new IllegalStateException("Invalid direction change X");
  }

  private static boolean goingLeft(Line line) {
    return precision.lt(line.getDirection().getX(), 0);
  }

  private static boolean goingRight(Line line) {
    return precision.gt(line.getDirection().getX(), 0);
  }

  private static boolean goingUp(Line line) {
    return precision.gt(line.getDirection().getY(), 0);
  }

  private static boolean goingDown(Line line) {
    return precision.lt(line.getDirection().getY(), 0);
  }

  private static boolean straightY(Line line) {
    return precision.eq(line.getDirection().getY(), 0);
  }

  private static boolean straightX(Line line) {
    return precision.eq(line.getDirection().getX(), 0);
  }

  public static Vector2D toVector2D(Point p) {
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
    return new PrevNext(prev, next);
  }

  public static boolean isConvex(LineSegment segment) {
    var curr = segment;
    do {
      var start = curr.getStart();
      var end = curr.getEnd();
      var nextPoint = curr.getNext().getEnd();
      if (orientationTest(start, end, nextPoint) == OrientationResult.RIGHT) {
        return false;
      }
      curr = curr.getNext();
    } while (curr != segment);
    return true;
  }

  public static boolean isReflexVertex(LineSegment prev, LineSegment next) {
    return orientationTest(prev.getStart(),prev.getEnd(),next.getEnd()) == OrientationResult.RIGHT;
  }

}
