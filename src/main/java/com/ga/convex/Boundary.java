package com.ga.convex;

import com.ga.data.BoundaryPoint;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.numbers.core.Precision;


@AllArgsConstructor
public class Boundary {
  private static final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-20);
  private BoundaryPoint source;
  private BoundaryPoint prev;

  public boolean sameBoundary(BoundaryPoint next) {
    boolean same = (source.isLeftBoundary() && isClockwise(next.point())) ||
        (!source.isLeftBoundary() && isAntiClockwise(next.point()));
    if (same) {
      prev = next;
    }
    return same;
  }

  private boolean isClockwise(Point p) {
    var prev = getLine(source.point(), this.prev.point());
    var next = getLine(source.point(), p);
    return prev.angle(next) < 0;
  }

  private boolean isAntiClockwise(Point p) {
    var prev = getLine(source.point(), this.prev.point());
    var next = getLine(source.point(), p);
    return prev.angle(next) > 0;
  }

  private Line getLine(Point one, Point two) {
    return Lines.fromPoints(GeometryUtil.toVector2D(one), GeometryUtil.toVector2D(two), precision);
  }
}
