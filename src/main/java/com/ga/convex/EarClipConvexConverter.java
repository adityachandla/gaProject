package com.ga.convex;

import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EarClipConvexConverter {
  private List<Point> allPoints;
  private List<LineSegment> createdSegments;
  private LineSegment curr;
  private LineSegment prev;

  public List<LineSegment> convertToConvexPolygons(LineSegment start) {
    createdSegments = new ArrayList<>();
    addAllPoints(start);
    curr = start;
    prev = start.getPrev();
    do {
      //While not convex, keep finding and clipping ears
      clipEar();
    } while (!GeometryUtil.isConvex(curr));
    createdSegments.add(curr);
    assignFaceIds(createdSegments);
    return createdSegments;
  }

  private void assignFaceIds(List<LineSegment> segments) {
    for (var segment: segments) {
      var curr = segment;
      var referenceId = FaceReferenceGenerator.getAndIncrementReferenceId();
      do {
        curr.setFaceReferenceId(referenceId);
        curr = curr.getNext();
      } while(curr != segment);
    }
  }

  private void clipEar() {
    while (true) {
      while (GeometryUtil.isReflexVertex(prev, curr)) {
        prev = curr;
        curr = curr.getNext();
      }
      var visibleSegment = new LineSegment(prev.getStart(), curr.getEnd());
      if (triangleContainsAnotherPoint(prev.getStart(), prev.getEnd(), curr.getEnd())) {
        prev = curr;
        curr = curr.getNext();
        continue;
      }
      //Doesn't contain any point, we can clip it off
      LineSegment triangleSegment = new LineSegment(curr.getEnd(), prev.getStart());

      triangleSegment.setSibling(visibleSegment);
      visibleSegment.setSibling(triangleSegment);

      triangleSegment.addReferenceToPoints();
      visibleSegment.addReferenceToPoints();

      visibleSegment.setNext(curr.getNext());
      visibleSegment.setPrev(prev.getPrev());

      triangleSegment.setNext(prev);
      triangleSegment.setPrev(curr);

      prev.getPrev().setNext(visibleSegment);
      curr.getNext().setPrev(visibleSegment);

      prev.setPrev(triangleSegment);
      curr.setNext(triangleSegment);

      prev = visibleSegment;
      curr = visibleSegment.getNext();
      createdSegments.add(triangleSegment);
      return;
    }
  }

  private boolean triangleContainsAnotherPoint(Point p1, Point p2, Point p3) {
    for (var point : allPoints) {
      if (point.equals(p1) || point.equals(p2) || point.equals(p3)) {
        continue;
      }
      //point should be left or collinear wrt all segments of the triangle
      var r1 = GeometryUtil.orientationTest(p1,p2, point) == GeometryUtil.OrientationResult.RIGHT;
      var r2 = GeometryUtil.orientationTest(p2,p3, point) == GeometryUtil.OrientationResult.RIGHT;
      var r3 = GeometryUtil.orientationTest(p3,p1, point) == GeometryUtil.OrientationResult.RIGHT;
      if (!r1 && !r2 && !r3) {
        return true;
      }
    }
    return false;
  }

  private void addAllPoints(LineSegment start) {
    allPoints = new ArrayList<>();
    var curr = start;
    do {
      allPoints.add(curr.getStart());
      curr = curr.getNext();
    } while (curr != start);
  }
}
