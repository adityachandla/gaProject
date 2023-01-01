package com.ga.triangulation;

import com.ga.data.BoundaryPoint;
import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class TriangulationUtil {

  public static List<LineSegment> triangulateYMonotone(LineSegment startSegment) {
    var boundaryPoints = getBoundaryPointsSorted(startSegment);
    var edgeGenerator = new TriangulationEdgeGenerator(boundaryPoints);
    var addedSegments = edgeGenerator.createTriangulation();
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
    int count = 0;
    int referenceId = FaceReferenceGenerator.getAndIncrementReferenceId();
    do {
      curr.setFaceReferenceId(referenceId);
      curr = curr.getNext();
      count++;
    } while (curr != segment);
    if (count != 3) {
      log.info("Something fucked up {}", count);
    }
  }

  private static List<BoundaryPoint> getBoundaryPointsSorted(LineSegment startSegment) {
    var topPoints = getTopPoint(startSegment);
    topPoints.sort(Comparator.comparingLong(Point::getX));
    var leftStart = GeometryUtil.getPrevNext(topPoints.get(0)).next();
    var rightStart = GeometryUtil.getPrevNext(topPoints.get(topPoints.size() - 1)).prev();
    List<BoundaryPoint> rightBoundary = getRightBoundaryPoints(rightStart);
    List<BoundaryPoint> leftBoundary = getLeftBoundaryPoints(leftStart);
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
      } else if (curr.getEnd().getY() == top.getY()) {
        topPoints.add(curr.getEnd());
      }
      curr = curr.getNext();
    } while (curr != start);
    return topPoints;
  }

}
