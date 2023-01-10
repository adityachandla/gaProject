package com.ga.triangulation;

import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class EarClipConvexConverter {
  private Set<Point> allPoints;
  private List<LineSegment> createdSegments;
  private LineSegment curr;
  private LineSegment prev;

  public List<LineSegment> convertToConvexPolygons(LineSegment start) {
    createdSegments = new ArrayList<>();
    addAllPoints(start);
    curr = start;
    prev = start.getPrev();
    while(!GeometryUtil.isConvex(curr)) {
      //While not convex, keep finding and clipping ears
      if (!clipEar()) {
        log.info("Curr is {} with face id {}", curr, curr.getFaceReferenceId());
        break;
      }
    }
    createdSegments.add(curr);
    if (createdSegments.size() > 1) {
      FaceReferenceGenerator.assignFaceIds(createdSegments);
    }
    return createdSegments;
  }

  private boolean clipEar() {
    var start = curr.getPrev();
    while (GeometryUtil.isReflexVertex(prev, curr) ||
        triangleContainsAnotherPoint(prev.getStart(), prev.getEnd(), curr.getEnd())) {
      if (start == curr) {
        return false;
      }
      prev = curr;
      curr = curr.getNext();
    }
    allPoints.remove(prev.getEnd());
    addSegments();
    return true;
  }

  private void addSegments() {
    var visibleSegment = new LineSegment(prev.getStart(), curr.getEnd());
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
    allPoints = new HashSet<>();
    var curr = start;
    do {
      allPoints.add(curr.getStart());
      curr = curr.getNext();
    } while (curr != start);
  }
}
