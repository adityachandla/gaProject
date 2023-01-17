package com.ga.monotone;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.PrevNext;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.numbers.core.Precision;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SegmentAdderUtil {
  private static final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-20);

  /**
   * We create lines from viewer to dest and from dest to viewer.
   * We check which PrevNext pair we get first if rotate the lineForPrev
   * in counterClockwise direction and the lineForNext in the clockwise
   * direction.
   *
   * @param dest   The point to which we need to create the line
   * @param viewer The point from which we need to create the line
   * @return The PrevNext pair that should be considered for @dest
   */
  public static PrevNext getPrevNextFromViewer(Point dest, Point viewer) {
    var prevNextPairs = getPrevNextPairs(dest);
    var lineForPrev = Lines.fromPoints(GeometryUtil.toVector2D(viewer), GeometryUtil.toVector2D(dest), precision);
    var lineForNext = Lines.fromPoints(GeometryUtil.toVector2D(dest), GeometryUtil.toVector2D(viewer), precision);
    double minAngle = 100;
    PrevNext minPrevNext = null;
    for (var prevNext : prevNextPairs) {
      double total = 0;
      total += getAngleCounterClockwise(lineForPrev, getLine(prevNext.prev()));
      total += getAngleClockwise(lineForNext, getLine(prevNext.next()));
      if (total < minAngle) {
        minAngle = total;
        minPrevNext = prevNext;
      }
    }
    return minPrevNext;
  }

  /**
   * By how many radians do we need to rotate one CCW to get to two.
   *
   * @param one viewing line
   * @param two one of the next lines
   * @return radians
   */
  private static double getAngleCounterClockwise(Line one, Line two) {
    var angle = one.angle(two);
    if (angle < 0) {
      angle += (2 * Math.PI);
    }
    return angle;
  }

  private static double getAngleClockwise(Line one, Line two) {
    var angle = one.angle(two);
    if (angle > 0) {
      angle -= (2 * Math.PI);
    }
    return Math.abs(angle);
  }


  private static Line getLine(LineSegment segment) {
    var start = GeometryUtil.toVector2D(segment.getStart());
    var end = GeometryUtil.toVector2D(segment.getEnd());
    return Lines.fromPoints(start, end, precision);
  }

  private static List<PrevNext> getPrevNextPairs(Point p) {
    //TODO This lies in the hot path and can be made more efficient
    List<PrevNext> pairs = new ArrayList<>();
    for (var one : p.getSegments()) {
      for (var two : p.getSegments()) {
        if (one.getNext() == two) {
          pairs.add(new PrevNext(one, two));
        }
      }
    }
    return pairs;
  }
}
