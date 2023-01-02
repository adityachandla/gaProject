package com.ga.triangulation;

import com.ga.data.BoundaryPoint;
import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.PrevNext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class TriangulationUtil {

  public static List<LineSegment> triangulateYMonotone(LineSegment startSegment) {
    var boundaryPoints = getBoundaryPoints(startSegment);
    var edgeGenerator = new TriangulationEdgeGenerator(boundaryPoints);
    //Verified that we are adding correct number of edges, and they are not repeated
    var addedSegments = edgeGenerator.getAddedEdges();
    var distinctSegments = new ArrayList<LineSegment>();
    var faceIdBefore = FaceReferenceGenerator.getCurrentReferenceId();
    for (var segment : addedSegments) {
      if (segment.getFaceReferenceId() <= faceIdBefore) {
        assignFaceId(segment);
        distinctSegments.add(segment);
      }
    }
    return distinctSegments;
  }

  private static void assignFaceId(LineSegment segment) {
    var curr = segment;
    int referenceId = FaceReferenceGenerator.getAndIncrementReferenceId();
    do {
      curr.setFaceReferenceId(referenceId);
      curr = curr.getNext();
    } while (curr != segment);
  }

  private static List<BoundaryPoint> getBoundaryPoints(LineSegment startSegment) {
    var topPoint = getTopPoint(startSegment);
    var bottomPoint = getBottomPoint(startSegment);
    List<BoundaryPoint> leftBoundary = new ArrayList<>();
    var start = topPoint.prevNext().next();
    while(start != bottomPoint.prevNext().prev()) {
      leftBoundary.add(BoundaryPoint.left(start.getStart()));
      start = start.getNext();
    }
    List<BoundaryPoint> rightBoundary = new ArrayList<>();
    start = topPoint.prevNext().prev();
    while (start != bottomPoint.prevNext.prev()) {
      rightBoundary.add(BoundaryPoint.right(start.getEnd()));
      start = start.getPrev();
    }
    return merge(leftBoundary, rightBoundary);
  }

  private static PointPrevNext getTopPoint(LineSegment start) {
    var top = new Point(0, Long.MIN_VALUE);
    var curr = start;
    LineSegment next = null;
    do {
      var p = curr.getStart();
      if (p.getY() > top.getY()) {
        top = p;
        next = curr;
      }
      top = p.getY() > top.getY() ? p : top;
    } while(curr != start);
    return new PointPrevNext(top, new PrevNext(next.getPrev(), next));
  }

  private static PointPrevNext getBottomPoint(LineSegment start) {
    var top = new Point(0, Long.MAX_VALUE);
    var curr = start;
    LineSegment next = null;
    do {
      var p = curr.getStart();
      if (p.getY() < top.getY()) {
        top = p;
        next = curr;
      }
    } while(curr != start);
    return new PointPrevNext(top, new PrevNext(next.getPrev(), next));
  }

  private static List<BoundaryPoint> getBoundaryPointsSorted(LineSegment startSegment) {
    var topPoints = getTopPoints(startSegment);
    topPoints.sort(Comparator.comparingLong(pPrevNext -> pPrevNext.p().getX()));
    var leftStart = topPoints.get(0).prevNext().next();
    var rightStart = topPoints.get(topPoints.size() - 1).prevNext().prev();
    List<BoundaryPoint> rightBoundary = getRightBoundary(rightStart);
    List<BoundaryPoint> leftBoundary = getLeftBoundary(leftStart);
    //If we only have one top point then we consider it in the right boundary
    //But if we have more than one point, we need to add the leftmost top
    //point in the left boundary.
    if (topPoints.size() > 1) {
      leftBoundary.add(0, BoundaryPoint.left(topPoints.get(0).p()));
    }
    return merge(leftBoundary, rightBoundary);
  }

  private static List<BoundaryPoint> merge(List<BoundaryPoint> left, List<BoundaryPoint> right) {
    left.addAll(right);
    Comparator<BoundaryPoint> pointComparator = Comparator
        .<BoundaryPoint>comparingLong(b -> b.point().getY())
        .reversed()
        .thenComparingLong(b -> b.point().getX());
    left.sort(pointComparator);
    return left;
  }

  private static List<BoundaryPoint> getRightBoundary(LineSegment start) {
    List<BoundaryPoint> rightBoundary = new ArrayList<>();
    rightBoundary.add(BoundaryPoint.right(start.getEnd()));
    for(var segment = start; onRightBoundary(segment); segment = segment.getPrev()) {
      rightBoundary.add(BoundaryPoint.right(segment.getStart()));
    }
    return rightBoundary;
  }

  private static boolean onRightBoundary(LineSegment segment) {
    //Going down -> yes, Going up -> no
    if (segment.getStart().getY() > segment.getEnd().getY()) {
      return false;
    } else if (segment.getStart().getY() < segment.getEnd().getY()) {
      return true;
    }
    //Going right -> yes, Going left -> no (because of plane rotation)
    if (segment.getStart().getX() < segment.getEnd().getX()) {
      return false;
    }
    return true;
  }

  private static List<BoundaryPoint> getLeftBoundary(LineSegment start) {
    List<BoundaryPoint> leftBoundary = new ArrayList<>();
    for(var segment = start; onLeftBoundary(segment); segment = segment.getNext()) {
      leftBoundary.add(BoundaryPoint.left(segment.getEnd()));
    }
    return leftBoundary;
  }

  private static boolean onLeftBoundary(LineSegment segment) {
    //Going down -> yes, Going up -> no
    if (segment.getStart().getY() > segment.getEnd().getY()) {
      return true;
    } else if (segment.getStart().getY() < segment.getEnd().getY()) {
      return false;
    }
    //If Y equal then:
    //Going right -> yes, Going left -> no (because of plane rotation)
    if (segment.getStart().getX() < segment.getEnd().getX()) {
      return true;
    }
    return false;
  }

  private static List<BoundaryPoint> getRightBoundaryPoints(LineSegment start) {
    List<BoundaryPoint> points = new ArrayList<>();
    points.add(BoundaryPoint.right(start.getEnd()));
    //We can check if we are going right or going left
    while (start.getStart().getY() < start.getEnd().getY()) {
      points.add(BoundaryPoint.right(start.getStart()));
      start = start.getPrev();
    }
    return points;
  }


  private static List<BoundaryPoint> getLeftBoundaryPoints(LineSegment start) {
    List<BoundaryPoint> points = new ArrayList<>();
    //There can be straight lines in between on the left boundary
    while (start.getStart().getY() >= start.getEnd().getY()) {
      points.add(BoundaryPoint.left(start.getEnd()));
      start = start.getNext();
    }
    while (points.size() > 2 && lastPointsHorizontal(points)) {
      points.remove(points.size() - 1);
    }
    return points;
  }

  private static boolean lastPointsHorizontal(List<BoundaryPoint> points) {
    var last = points.get(points.size() - 1);
    var secondLast = points.get(points.size() - 2);
    return last.point().getY() == secondLast.point().getY();
  }

  private static List<PointPrevNext> getTopPoints(LineSegment start) {
    //If the top of the polygon is flat, there may be multiple top points
    //We need the point as well as its prevNext segments in the current polygon
    var topPoints = new ArrayList<PointPrevNext>();
    var top = start.getStart();
    topPoints.add(new PointPrevNext(top, new PrevNext(start.getPrev(), start)));
    var curr = start;
    do {
      if (curr.getEnd().getY() > top.getY()) {
        topPoints.clear();
        topPoints.add(new PointPrevNext(curr.getEnd(), new PrevNext(curr, curr.getNext())));
        top = curr.getEnd();
      } else if (curr.getEnd().getY() == top.getY()) {
        topPoints.add(new PointPrevNext(curr.getEnd(), new PrevNext(curr, curr.getNext())));
      }
      curr = curr.getNext();
    } while (curr != start);
    return topPoints;
  }

  private record PointPrevNext(Point p, PrevNext prevNext) {
  }

}
