package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConvexConverter {

  public static List<LineSegment> convertToConvexPolygons(Polygon polygon) {
    List<LineSegment> segmentsToAdd = YMonotoneConverter.getSegmentsToMakeYMonotone(polygon);
    log.info("Adding {} lines to make it Y-monotone", segmentsToAdd.size());
    List<LineSegment> polygons = SegmentAdder.addLineSegments(segmentsToAdd);
    log.info("Got {} distinct polygons", polygons.size());
    List<LineSegment> convexPolygons = new ArrayList<>(polygons.size()); //We assume the order is CCW.
    for (var polygonStartSegment : polygons) {
      if(isConvex(polygonStartSegment)) {
        convexPolygons.add(polygonStartSegment);
      } else {
        List<LineSegment> triangleSegments = TriangulationUtil.triangulateYMonotone(polygonStartSegment);
        log.info("Created a triangulation of {} triangles", triangleSegments.size());
        convexPolygons.addAll(triangleSegments);
      }
    }
    log.info("Returning {} convex polygons", convexPolygons.size());
    return convexPolygons;
  }

  private static boolean isConvex(LineSegment segment) {
    var curr = segment;
    do {
      var start = curr.getStart();
      var end = curr.getEnd();
      var nextPoint = curr.getNext().getEnd();
      if (GeometryUtil.orientationTest(start, end, nextPoint) == GeometryUtil.OrientationResult.RIGHT) {
        return false;
      }
      curr = curr.getNext();
    } while (curr != segment);
    return true;
  }
}

