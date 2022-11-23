package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import com.ga.data.ProblemInstance;

import java.util.List;
import java.util.stream.Collectors;

public class PolygonCreationUtil {

  //TODO write tests
  public static Polygon createPolygon(ProblemInstance problemInstance) {
    var outerBoundary = problemInstance.outerBoundary();
    //Doubly connected edge list for polygon and holes
    var boundaryStartSegment = createBoundary(outerBoundary);
    var holes = problemInstance.holes().stream()
        .map(PolygonCreationUtil::createBoundary)
        .toList();
    //List of all points
    var polygonPoints = problemInstance.holes().stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
    polygonPoints.addAll(problemInstance.outerBoundary());
    return new Polygon(polygonPoints, boundaryStartSegment, holes);
  }

  private static LineSegment createBoundary(List<Point> boundary) {
    assert boundary.size() > 2;
    var startSegment = new LineSegment(boundary.get(0), boundary.get(1));
    var curr = startSegment;
    for (int i = 2; i < boundary.size(); i++) {
      var newSegment = new LineSegment(boundary.get(i-1), boundary.get(i));
      curr.setNext(newSegment);
      newSegment.setPrev(curr);
      curr = newSegment;

      //We also maintain a reference from a point to a segments that it is a part of
      boundary.get(i-1).getSegments().add(newSegment);
      boundary.get(i).getSegments().add(newSegment);
    }
    return startSegment;
  }
}
