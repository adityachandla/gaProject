package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Polygon;
import com.ga.data.Point;
import com.ga.monotone.SegmentAdder;
import com.ga.monotone.YMonotoneConverter;
import com.ga.triangulation.TriangulationUtil;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Slf4j
public class ConvexConverter {

  public static List<LineSegment> convertToConvexPolygons(Polygon polygon) {
    List<LineSegment> segmentsToAdd = YMonotoneConverter.getSegmentsToMakeYMonotone(polygon);
    log.info("Adding {} lines to make it Y-monotone", segmentsToAdd.size());
    List<LineSegment> polygons = SegmentAdder.addLineSegments(segmentsToAdd);
    log.info("Got {} polygons", polygons.size());
    Checker.checkYMonotone(polygon, polygons);
    List<LineSegment> convexPolygons = new ArrayList<>();
    for (var p : polygons) {
      if (GeometryUtil.isConvex(p)) {
        convexPolygons.add(p);
      } else {
        var earClipper = new EarClipConvexConverter();
        convexPolygons.addAll(earClipper.convertToConvexPolygons(p));
      }
    }
    log.info("Got {} convex polygons", convexPolygons.size());
    return convexPolygons;
  }
}

