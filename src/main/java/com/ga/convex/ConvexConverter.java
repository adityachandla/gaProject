package com.ga.convex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import com.ga.data.PrevNext;
import com.ga.monotone.SegmentAdder;
import com.ga.monotone.YMonotoneConverter;
import com.ga.triangulation.EarClipConvexConverter;
import com.ga.util.GeometryUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    outputSegments(convexPolygons);
    var minimalSegments = PolygonMerger.mergePolygons(convexPolygons);
    Checker.checkYMonotone(polygon, minimalSegments);
    log.info("Got {} convex polygons after reduction", minimalSegments.size());
    return minimalSegments;
  }

  private record Line(Point start, Point end) {}
  @SneakyThrows
  private static void outputSegments(List<LineSegment> segments) {
    var mapper = new ObjectMapper();
    var lines = segments.stream().map(ls -> new Line(ls.getStart(), ls.getEnd())).toList();

    var file = new File("/home/aditya/Downloads/TUE/GA/grapher/lines.json");
    var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    writer.write(mapper.writeValueAsString(lines));
    writer.close();
  }

}

