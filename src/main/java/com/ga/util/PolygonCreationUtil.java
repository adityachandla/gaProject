package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import com.ga.data.ProblemInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PolygonCreationUtil {

  public static Polygon createPolygon(ProblemInstance problemInstance) {
    var outerBoundary = problemInstance.outerBoundary();
    var boundaryStartSegment = createBoundary(outerBoundary);
    var holes = problemInstance.holes().stream()
        .map(PolygonCreationUtil::createBoundary)
        .toList();

    var polygonPoints = problemInstance.holes().stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
    polygonPoints.addAll(problemInstance.outerBoundary());
    return new Polygon(polygonPoints, boundaryStartSegment, holes);
  }

  private static LineSegment createBoundary(List<Point> boundary) {
    assert boundary.size() > 2;
    var startSegment = new LineSegment(boundary.get(0), boundary.get(1));
    startSegment.addReferenceToPoints();
    var curr = startSegment;
    for (int i = 2; i < boundary.size(); i++) {
      var newSegment = new LineSegment(boundary.get(i - 1), boundary.get(i));
      newSegment.addReferenceToPoints();
      linkSegments(curr, newSegment);
      curr = newSegment;
    }
    var connectorSegment = new LineSegment(boundary.get(boundary.size() - 1), boundary.get(0));
    connectorSegment.addReferenceToPoints();
    linkSegments(curr, connectorSegment);
    linkSegments(connectorSegment, startSegment);
    return startSegment;
  }

  private static void linkSegments(LineSegment from, LineSegment to) {
    from.setNext(to);
    to.setPrev(from);
  }
}
