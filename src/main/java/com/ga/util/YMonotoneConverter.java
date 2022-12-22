package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class YMonotoneConverter {

  public static List<LineSegment> getSegmentsToMakeYMonotone(Polygon polygon) {
    //Going from top to bottom and left to right
    var comparator = Comparator.comparingInt(Point::getY)
        .reversed()
        .thenComparingInt(Point::getX);
    polygon.getPoints().sort(comparator);

    //Sort lines on the basis of x coordinate of starting point
    var leftLines = new TreeSet<LineSegment>(Comparator.comparingInt(line -> line.getStart().getX()));
    var helper = new HashMap<LineSegment, Point>();

    var lineSegmentsToAdd = new ArrayList<LineSegment>();
    for (var point : polygon.getPoints()) {
      var pointType = GeometryUtil.getPointType(point);
      switch (pointType) {
        case SPLIT -> lineSegmentsToAdd.add(handleSplit(point, leftLines, helper));
        case MERGE -> lineSegmentsToAdd.addAll(handleMerge(point, leftLines, helper));
        case START -> handleStart(point, leftLines, helper);
        case END -> lineSegmentsToAdd.addAll(handleEnd(point, leftLines, helper));
        case REGULAR -> lineSegmentsToAdd.addAll(handleRegular(point, leftLines, helper));
      }
    }
    return lineSegmentsToAdd;
  }

  private static LineSegment handleSplit(Point point, TreeSet<LineSegment> leftLines, HashMap<LineSegment, Point> helper) {
    var lineToLeft = leftLines.lower(new LineSegment(point, null));
    var segmentCreated = new LineSegment(point, helper.get(lineToLeft));
    helper.put(lineToLeft, point);

    var rightEdge = GeometryUtil.getPrevNext(point).next();
    leftLines.add(rightEdge);
    helper.put(rightEdge, point);
    return segmentCreated;
  }

  private static List<LineSegment> handleMerge(Point point,
                                               TreeSet<LineSegment> leftLines,
                                               HashMap<LineSegment, Point> helper) {
    var linesAdded = new ArrayList<LineSegment>(2);
    var rightEdge = GeometryUtil.getPrevNext(point).prev();
    var rightCandidate = helper.get(rightEdge);
    if (GeometryUtil.getPointType(rightCandidate) == GeometryUtil.PointType.MERGE) {
      linesAdded.add(new LineSegment(point, rightCandidate));
    }
    leftLines.remove(rightEdge);

    var lineToLeft = leftLines.lower(new LineSegment(point, null));
    var leftCandidate = helper.get(lineToLeft);
    if (GeometryUtil.getPointType(leftCandidate) == GeometryUtil.PointType.MERGE) {
      linesAdded.add(new LineSegment(point, leftCandidate));
    }
    helper.put(lineToLeft, point);
    return linesAdded;
  }

  private static void handleStart(Point point, TreeSet<LineSegment> leftLines, HashMap<LineSegment, Point> helper) {
    var prevNext = GeometryUtil.getPrevNext(point);
    var leftEdge = prevNext.next();
    leftLines.add(leftEdge);
    helper.put(leftEdge, point);
  }

  private static List<LineSegment> handleEnd(Point point, TreeSet<LineSegment> leftLines, HashMap<LineSegment, Point> helper) {
    var linesAdded = new ArrayList<LineSegment>(1);
    var prevNext = GeometryUtil.getPrevNext(point);
    var leftEdge = prevNext.prev();
    var candidate = helper.get(leftEdge);
    assert candidate != null;
    if (GeometryUtil.getPointType(candidate) == GeometryUtil.PointType.MERGE) {
      linesAdded.add(new LineSegment(candidate, point));
    }
    leftLines.remove(leftEdge);
    return linesAdded;
  }

  private static List<LineSegment> handleRegular(Point point,
                                                 TreeSet<LineSegment> leftLines,
                                                 HashMap<LineSegment, Point> helper) {
    var linesAdded = new ArrayList<LineSegment>(1);
    if (isPolygonToTheRight(point)) {
      var upperLower = getUpperLower(point);
      //Handle lower
      leftLines.add(upperLower.lower());
      helper.put(upperLower.lower(), point);

      //Handle upper
      var upperHelper = helper.get(upperLower.upper());
      if (GeometryUtil.getPointType(upperHelper) == GeometryUtil.PointType.MERGE) {
        linesAdded.add(new LineSegment(upperHelper, point));
      }
      leftLines.remove(upperLower.upper());
    } else {
      var leftLine = leftLines.lower(new LineSegment(point, null));
      var leftLineHelper = helper.get(leftLine);
      if (GeometryUtil.getPointType(leftLineHelper) == GeometryUtil.PointType.MERGE) {
        linesAdded.add(new LineSegment(leftLineHelper, point));
      }
      helper.put(leftLine, point);
    }
    return linesAdded;
  }

  /**
   * In a hole if we are going up within a hole then polygon is to the right
   * If we are going down in the outer boundary then the polygon is to the right
   *
   * @param point A point of polygon or hole
   * @return is the polygon to the right
   */
  private static boolean isPolygonToTheRight(Point point) {
    var prevNext = GeometryUtil.getPrevNext(point);
    return prevNext.next().getEnd().getY() <= point.getY();
  }

  //TODO handle degenerate cases
  private static UpperLower getUpperLower(Point p) {
    var prevNext = GeometryUtil.getPrevNext(p);
    int prevY = prevNext.prev().getStart().getY();
    int nextY = prevNext.next().getEnd().getY();
    if (prevY > nextY) {
      return new UpperLower(prevNext.prev(), prevNext.next());
    }
    return new UpperLower(prevNext.next(), prevNext.prev());
  }

  private record UpperLower(LineSegment upper, LineSegment lower) {
  }
}
