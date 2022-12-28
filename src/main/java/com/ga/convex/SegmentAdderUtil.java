package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import com.ga.util.PrevNext;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.numbers.core.Precision;

import java.util.List;

public class SegmentAdderUtil {
  private static final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-20);
  public static PrevNext getFirstPairClockwise(List<PrevNext> pairs, Point p) {
    var leftPoint = new Point(0, p.getY());
    var leftLine = Lines.fromPoints(GeometryUtil.toVector2D(p), GeometryUtil.toVector2D(leftPoint), precision);
    double minAngle = 100d;
    PrevNext minPair = null;
    for (var pair : pairs) {
      var line = getLine(pair.next());
      double angle = leftLine.angle(line);
      if(precision.lt(angle, 0))  {
        angle += 2*Math.PI;
      }
      if (angle < minAngle) {
        minAngle = angle;
        minPair = pair;
      }
    }
    return minPair;
  }

  private static Line getLine(LineSegment segment) {
    var start = GeometryUtil.toVector2D(segment.getStart());
    var end = GeometryUtil.toVector2D(segment.getEnd());
    return Lines.fromPoints(start, end, precision);
  }
}
