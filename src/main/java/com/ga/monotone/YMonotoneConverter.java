package com.ga.monotone;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Slf4j
public class YMonotoneConverter {

  private final TreeSet<LineSegment> leftLines;
  private final Map<LineSegment, Point> helper = new HashMap<>();
  private final List<LineSegment> lineSegmentsToAdd = new ArrayList<>();
  private final List<Point> polygonPoints;

  public YMonotoneConverter(Polygon polygon) {
    //Going from top to bottom and left to right
    var comparator = Comparator.comparingLong(Point::getY)
        .reversed()
        .thenComparingLong(Point::getX);
    polygon.getPoints().sort(comparator);

    polygonPoints = polygon.getPoints();
    leftLines = new TreeSet<>(Comparator.comparingLong(line -> line.getStart().getX()));
  }

  public List<LineSegment> getSegmentsToMakeYMonotone() {
    for (var point : polygonPoints) {
      switch (GeometryUtil.getPointType(point)) {
        case SPLIT -> handleSplit(point);
        case MERGE -> handleMerge(point);
        case START -> handleStart(point);
        case END -> handleEnd(point);
        case REGULAR -> handleRegular(point);
      }
    }
    return lineSegmentsToAdd;
  }

  private void handleSplit(Point point) {
    var lineToLeft = getSegmentToLeft(point);
    var segmentCreated = new LineSegment(point, helper.get(lineToLeft));
    lineSegmentsToAdd.add(segmentCreated);
    helper.put(lineToLeft, point);

    var rightEdge = GeometryUtil.getPrevNext(point).next();
    leftLines.add(rightEdge);
    helper.put(rightEdge, point);
  }

  private void handleMerge(Point point) {
    var rightEdge = GeometryUtil.getPrevNext(point).prev();
    var rightCandidate = helper.get(rightEdge);
    if (GeometryUtil.getPointType(rightCandidate) == GeometryUtil.PointType.MERGE) {
      lineSegmentsToAdd.add(new LineSegment(point, rightCandidate));
    }
    leftLines.remove(rightEdge);

    var lineToLeft = getSegmentToLeft(point);
    var leftCandidate = helper.get(lineToLeft);
    if (GeometryUtil.getPointType(leftCandidate) == GeometryUtil.PointType.MERGE) {
      lineSegmentsToAdd.add(new LineSegment(point, leftCandidate));
    }
    helper.put(lineToLeft, point);
  }

  private void handleStart(Point point) {
    var prevNext = GeometryUtil.getPrevNext(point);
    var leftEdge = prevNext.next();
    leftLines.add(leftEdge);
    helper.put(leftEdge, point);
  }

  private void handleEnd(Point point) {
    var prevNext = GeometryUtil.getPrevNext(point);
    var leftEdge = prevNext.prev();
    var candidate = helper.get(leftEdge);
    if (GeometryUtil.getPointType(candidate) == GeometryUtil.PointType.MERGE) {
      lineSegmentsToAdd.add(new LineSegment(point, candidate));
    }
    leftLines.remove(leftEdge);
  }

  private void handleRegular(Point point) {
    if (canJoinToRight(point)) {
      var prevNext = GeometryUtil.getPrevNext(point);
      var upper = prevNext.prev();
      var lower = prevNext.next();
      //Handle upper
      var upperHelper = helper.get(upper);
      if (GeometryUtil.getPointType(upperHelper) == GeometryUtil.PointType.MERGE) {
        lineSegmentsToAdd.add(new LineSegment(point, upperHelper));
      }
      leftLines.remove(upper);

      //Handle lower
      leftLines.add(lower);
      helper.put(lower, point);
    } else {
      var leftLine = getSegmentToLeft(point);
      var leftLineHelper = helper.get(leftLine);
      if (GeometryUtil.getPointType(leftLineHelper) == GeometryUtil.PointType.MERGE) {
        lineSegmentsToAdd.add(new LineSegment(point, leftLineHelper));
      }
      helper.put(leftLine, point);
    }
  }

  /**
   * @param point A point of polygon or hole
   * @return boolean is the polygon to the right
   */
  private static boolean canJoinToRight(Point point) {
    var prevNext = GeometryUtil.getPrevNext(point);
    return isBelow(prevNext.next().getEnd(), point);
  }

  /**
   * @param next    Next point
   * @param current current point
   * @return if next is below curr
   */
  private static boolean isBelow(Point next, Point current) {
    if (next.getY() < current.getY()) {
      return true;
    } else if (next.getY() > current.getY()) {
      return false;
    }
    return next.getX() > current.getX();
  }

  /**
   * Our tree is based on x coordinate of the start of the segment. This is not
   * always a point to the left although the line may be to the left. This is a
   * hack to take care of the edge case.
   *
   * @param p Point
   * @return Line segment that is to the left
   */
  private LineSegment getSegmentToLeft(Point p) {
    var descendingIterator = leftLines.descendingIterator();
    while (descendingIterator.hasNext()) {
      var segment = descendingIterator.next();
      if (isLineToTheLeft(p, segment)) {
        return segment;
      }
    }
    throw new IllegalStateException("No line found");
  }

  private static boolean isLineToTheLeft(Point p, LineSegment segment) {
    var higher = segment.getStart();
    var lower = segment.getEnd();
    if (lower.getY() > higher.getY() || (lower.getY() == higher.getY() && lower.getX() > higher.getX())) {
      var temp = higher;
      higher = lower;
      lower = temp;
    }
    return GeometryUtil.orientationTest(lower, higher, p) == GeometryUtil.OrientationResult.RIGHT;
  }
}
