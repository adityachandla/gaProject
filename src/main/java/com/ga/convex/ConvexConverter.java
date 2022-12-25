package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Polygon;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ConvexConverter {

  public static void convertToConvexPolygons(Polygon polygon) {
    List<LineSegment> segmentsToAdd = YMonotoneConverter.getSegmentsToMakeYMonotone(polygon);
    log.info("Adding {} lines to make it Y-monotone", segmentsToAdd.size());
    List<LineSegment> polygons = SegmentAdder.addLineSegments(segmentsToAdd);
  }

}

