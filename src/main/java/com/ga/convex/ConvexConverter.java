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
    outputSegments(segmentsToAdd);
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
    mergePolygons(convexPolygons);
    var minimalPolygons = convexPolygons.stream()
        .filter(line -> line.getFaceReferenceId() != -1)
        .toList();
    log.info("Got {} convex polygons after reduction", minimalPolygons.size());
    return minimalPolygons;
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

  private static void mergePolygons(List<LineSegment> addedSegments) {
    for (var segment : addedSegments) {
      if (segment.getFaceReferenceId() == -1 || segment.getSibling() == null) {
        continue;
      }
      var polygonSegmentOpt = getPrivateLineSegment(segment);
      if (polygonSegmentOpt.isEmpty()) {
        continue;
      }
      var polygonSegment = polygonSegmentOpt.get();
      var edgeRemover = new EdgeRemover(segment);
      edgeRemover.softDelete();
      assert polygonSegment.getPrev() != segment;
      if (GeometryUtil.isConvex(polygonSegment)) {
        FaceReferenceGenerator.assignFaceId(polygonSegment);
        edgeRemover.hardDelete();
      } else {
        edgeRemover.revertSoftDelete();
      }
    }
  }

  private static Optional<LineSegment> getPrivateLineSegment(LineSegment start) {
    var curr = start;
    do {
      curr = curr.getNext();
      if (curr.getSibling() == null) {
        return Optional.of(curr);
      }
    } while (curr != start);
    return Optional.empty();
  }

  private static class EdgeRemover {
    private LineSegment segment;
    private LineSegment sibling;
    private PrevNext segmentPrevNext;
    private PrevNext siblingPrevNext;

    EdgeRemover(LineSegment segment) {
      this.segment = segment;
      assert segment.getSibling() != null;
      this.sibling = segment.getSibling();
    }

    public void softDelete() {
      assert segmentPrevNext == null;
      assert siblingPrevNext == null;
      segmentPrevNext = new PrevNext(segment.getPrev(), segment.getNext());
      siblingPrevNext = new PrevNext(sibling.getPrev(), sibling.getNext());

      segment.getPrev().setNext(siblingPrevNext.next());
      segment.getNext().setPrev(siblingPrevNext.prev());

      sibling.getNext().setPrev(segmentPrevNext.prev());
      sibling.getPrev().setNext(segmentPrevNext.next());
      assert segmentPrevNext.prev().getNext() == siblingPrevNext.next();
      assert siblingPrevNext.prev().getNext() == segmentPrevNext.next();

      assert segmentPrevNext.next().getPrev() == siblingPrevNext.prev();
      assert siblingPrevNext.next().getPrev() == segmentPrevNext.prev();
    }

    public void hardDelete() {
      segment.removeReferenceFromPoints();
      sibling.removeReferenceFromPoints();
      segment.setNext(null);
      segment.setFaceReferenceId(-1);
      segment.setPrev(null);
      sibling.setPrev(null);
      sibling.setFaceReferenceId(-1);
      sibling.setNext(null);
    }

    public void revertSoftDelete() {
      assert segmentPrevNext != null;
      assert siblingPrevNext != null;
      segmentPrevNext.next().setPrev(segment);
      segmentPrevNext.prev().setNext(segment);

      siblingPrevNext.next().setPrev(sibling);
      siblingPrevNext.prev().setNext(sibling);
    }
  }
}

