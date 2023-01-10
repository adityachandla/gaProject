package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Polygon;
import com.ga.monotone.SegmentAdder;
import com.ga.monotone.YMonotoneConverter;
import com.ga.triangulation.EarClipConvexConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConvexConverter {

  public static List<LineSegment> convertToConvexPolygons(Polygon polygon) {
    var monotoneConverter = new YMonotoneConverter(polygon);
    List<LineSegment> segmentsToAdd = monotoneConverter.getSegmentsToMakeYMonotone();
    log.info("Adding {} lines to make it Y-monotone", segmentsToAdd.size());
    List<LineSegment> polygons = SegmentAdder.addLineSegments(segmentsToAdd);
    log.info("Got {} polygons", polygons.size());
    Checker.checkYMonotone(polygon, polygons);
    List<LineSegment> convexPolygons = new ArrayList<>();
    for (var p : polygons) {
      var earClipper = new EarClipConvexConverter();
      convexPolygons.addAll(earClipper.convertToConvexPolygons(p));
    }
    log.info("Got {} convex polygons before reduction", convexPolygons.size());
    mergePolygons(segmentsToAdd);
    return convexPolygons;
  }

  private static void mergePolygons(List<LineSegment> addedSegments) {
    //For each segment try to merge the two polygons.
  }
}

