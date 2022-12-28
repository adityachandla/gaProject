package com.ga.convex;

import com.ga.data.BoundaryPoint;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

@Slf4j
public class TriangulationUtil {

  public static List<LineSegment> triangulateYMonotone(LineSegment startSegment) {
    var boundaryPoints = getBoundaryPointsSorted(startSegment);
    var addedSegments = createTriangulation(boundaryPoints);
    var distinctSegments = new ArrayList<LineSegment>();
    var faceIdBefore = FaceReferenceGenerator.getCurrentReferenceId();
    for (var segment : addedSegments) {
      if (segment.getFaceReferenceId() > faceIdBefore) {
        continue;
      }
      assignFaceId(segment);
      distinctSegments.add(segment);
    }
    return distinctSegments;
  }

  private static void assignFaceId(LineSegment segment) {
    var curr = segment;
    int count = 0;
    do {
      curr.setFaceReferenceId(FaceReferenceGenerator.getAndIncrementReferenceId());
      curr = curr.getNext();
      count++;
    } while(curr != segment);
    if (count != 3) {
      log.info("Something fucked up {}", count);
    }
  }

  private static List<LineSegment> createTriangulation(List<BoundaryPoint> boundaryPoints) {
    List<LineSegment> addedSegments = new ArrayList<>(boundaryPoints.size());
    var stack = new Stack<BoundaryPoint>();
    stack.add(boundaryPoints.get(0));
    stack.add(boundaryPoints.get(1));
    for (int i = 2; i < boundaryPoints.size()-1; i++) {
      var top = stack.peek();
      var v = boundaryPoints.get(i);
      if (v.isLeftBoundary() ^ top.isLeftBoundary()) {
        //Different boundaries
        BoundaryPoint lastProcessed = null;
        while (stack.size() > 1) {
          var toProcess = stack.pop();
          lastProcessed = toProcess;
          var addedSegment = addSegment(v, toProcess);
          addedSegments.add(addedSegment);
        }
        stack.pop();
        stack.push(lastProcessed);
        stack.push(v);
      } else {
        var popped = stack.pop();
        var prev = popped;
        var boundary = new Boundary(v, popped);
        while (!stack.empty() && boundary.sameBoundary(stack.peek())) {
          var toProcess = stack.pop();
          prev = toProcess;
          var addedSegment = addSegment(v, toProcess);
          addedSegments.add(addedSegment);
        }
        stack.push(prev);
        stack.push(v);
      }
    }
    var last = boundaryPoints.get(boundaryPoints.size()-1);
    if (!stack.empty()) stack.pop();
    while (stack.size() > 1) {
      var toProcess = stack.pop();
      var addedSegment = addSegment(last, toProcess);
      addedSegments.add(addedSegment);
    }
    return addedSegments;
  }

  private static LineSegment addSegment(BoundaryPoint one, BoundaryPoint two) {
    if (one.isLeftBoundary()) {
      var temp = one;
      one = two;
      two = temp;
    }
    return SegmentAdder.createSegment(one.point(), two.point());
  }

  private static List<BoundaryPoint> getBoundaryPointsSorted(LineSegment startSegment) {
    var topPoints = getTopPoint(startSegment);
    topPoints.sort(Comparator.comparingLong(Point::getX));
    var leftStart = GeometryUtil.getPrevNext(topPoints.get(0)).next();
    var rightStart = GeometryUtil.getPrevNext(topPoints.get(topPoints.size()-1)).prev();
    List<BoundaryPoint> rightBoundary = getRightBoundaryPoints(rightStart);
    List<BoundaryPoint> leftBoundary = getLeftBoundaryPoints(leftStart);
    List<BoundaryPoint> boundaryPoints = merge(leftBoundary, rightBoundary);
    return boundaryPoints;
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

  private static List<BoundaryPoint> getRightBoundaryPoints(LineSegment start) {
    List<BoundaryPoint> points = new ArrayList<>();
    points.add(BoundaryPoint.right(start.getEnd()));
    while (start.getStart().getY() < start.getEnd().getY()) {
      points.add(BoundaryPoint.right(start.getStart()));
      start = start.getPrev();
    }
    return points;
  }


  private static List<BoundaryPoint> getLeftBoundaryPoints(LineSegment start) {
    List<BoundaryPoint> points = new ArrayList<>();
    points.add(BoundaryPoint.left(start.getStart()));
    //There can be straight lines in between on the left boundary
    while (start.getStart().getY() >= start.getEnd().getY()) {
      points.add(BoundaryPoint.left(start.getEnd()));
      start = start.getNext();
    }
    while (points.size() > 2  && lastPointsHorizontal(points)) {
      points.remove(points.size()-1);
    }
    return points;
  }

  private static boolean lastPointsHorizontal(List<BoundaryPoint> points) {
    var last = points.get(points.size()-1);
    var secondLast = points.get(points.size()-2);
    return last.point().getY() == secondLast.point().getY();
  }

  private static List<Point> getTopPoint(LineSegment start) {
    var topPoints = new ArrayList<Point>();
    var top = start.getStart();
    topPoints.add(top);
    var curr = start;
    do {
      if (curr.getEnd().getY() > top.getY()) {
        topPoints.clear();
        topPoints.add(curr.getEnd());
        top = curr.getEnd();
      } else if(curr.getEnd().getY() == top.getY()) {
        topPoints.add(curr.getEnd());
      }
      curr = curr.getNext();
    } while(curr != start);
    return topPoints;
  }


}
