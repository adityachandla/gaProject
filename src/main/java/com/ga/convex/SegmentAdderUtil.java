package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import com.ga.util.PrevNext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.numbers.core.Precision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class SegmentAdderUtil {
  private static final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-20);

  public static PrevNext getPrevNextFromViewer3(Point dest, Point viewer) {
    boolean viewerLookingRight = isViewerLookingRight(dest, viewer);
    boolean viewerLookingDown = isViewerLookingDown(dest, viewer);
    var prevNextPairs = getPrevNextPairs(dest);
    var lineForPrev = Lines.fromPoints(GeometryUtil.toVector2D(viewer), GeometryUtil.toVector2D(dest), precision);
    var lineForNext = Lines.fromPoints(GeometryUtil.toVector2D(dest), GeometryUtil.toVector2D(viewer), precision);
    double minAngle = 100;
    PrevNext minPrevNext = null;
    for (var prevNext : prevNextPairs) {
      double total = 0;
      if (viewerLookingRight & viewerLookingDown) {
        total += getAngleCounterClockwise(getLine(prevNext.prev()), lineForPrev);
        total += getAngleCounterClockwise(lineForNext, getLine(prevNext.next()));
      }else {
        total += getAngleCounterClockwise(lineForPrev, getLine(prevNext.prev()));
        total += getAngleCounterClockwise(getLine(prevNext.next()), lineForNext);
      }
      if (total < minAngle) {
        minAngle = total;
        minPrevNext = prevNext;
      }
    }
    return minPrevNext;
  }

  public static PrevNext getPrevNextFromViewer2(Point dest, Point viewer) {
    var prevNextPairs = getPrevNextPairs(dest);
    var line = Lines.fromPoints(GeometryUtil.toVector2D(dest), GeometryUtil.toVector2D(viewer), precision);
    if (isViewerLookingRight(dest, viewer)) {
      double minAngle = 100; //Will always be between 0 and 2*PI.
      PrevNext minPair = null;
      for (var pair : prevNextPairs) {
        double angle = getAngleCounterClockwise(line, getLine(pair.next()));
        if (angle < minAngle) {
          minPair = pair;
          minAngle = angle;
        }
      }
      return minPair;
    }
    double minAngle = 100; //Will always be between 0 and 2*PI.
    PrevNext minPair = null;
    for (var pair : prevNextPairs) {
      double angle = getAngleCounterClockwise(getLine(pair.next()), line);
      if (angle < minAngle) {
        minPair = pair;
        minAngle = angle;
      }
    }
    return minPair;
  }

  private static boolean isViewerLookingRight(Point dest, Point viewer) {
    if (dest.getX() > viewer.getX()) {
      return true;
    } else if(dest.getX() < viewer.getX()) {
      return false;
    }
    //both x are same, if looking up then yes, if looking down then no
    return dest.getY() > viewer.getY();
  }

  private static boolean isViewerLookingDown(Point dest, Point viewer) {
    if (dest.getY() < viewer.getY()) {
      return true;
    } else if(viewer.getY() < dest.getY()) {
      return false;
    }
    //Both y are same, if looking right then yes, if left then no
    return dest.getX() > viewer.getX();
  }

  /**
   * By how many radians do we need to rotate CCW one to get to two.
   * @param one viewing line
   * @param two one of the next lines
   * @return radians
   */
  private static double getAngleCounterClockwise(Line one, Line two) {
    var angle = one.angle(two);
    if(angle < 0) {
      return angle + (2*Math.PI);
    }
    return angle;
  }

  public static PrevNext getPrevNextFromViewer(Point dest, Point viewer) {
    var prevNextPairs = getPrevNextPairs(dest);
    var line = Lines.fromPoints(GeometryUtil.toVector2D(viewer), GeometryUtil.toVector2D(dest), precision);
    var comparator = new AngleComparator(line);
    prevNextPairs.sort(comparator);
    return prevNextPairs.get(0);
  }

  @AllArgsConstructor
  private static class AngleComparator implements Comparator<PrevNext> {
    private Line line;

    @Override
    public int compare(PrevNext one, PrevNext two) {
      var oneNextAngle = Math.abs(getLine(one.next()).angle(line));
      var twoNextAngle = Math.abs(getLine(two.next()).angle(line));
      if (precision.lt(oneNextAngle, twoNextAngle)) {
        return 1;
      } else if(precision.eq(oneNextAngle, twoNextAngle)) {
        var onePrevAngle = Math.abs(getLine(one.prev()).angle(line));
        var twoPrevAngle = Math.abs(getLine(two.prev()).angle(line));
        return precision.lt(onePrevAngle, twoPrevAngle) ? 1 : -1;
      }
      return -1;
    }

  }


  private static Line getLine(LineSegment segment) {
    var start = GeometryUtil.toVector2D(segment.getStart());
    var end = GeometryUtil.toVector2D(segment.getEnd());
    return Lines.fromPoints(start, end, precision);
  }

  private static List<PrevNext> getPrevNextPairs(Point p) {
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
