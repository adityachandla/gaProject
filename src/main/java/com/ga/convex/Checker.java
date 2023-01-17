package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
public class Checker {

  public static void checkPolygons(Polygon initialPolygon, List<LineSegment> startSegments) {
    int expectedSize = initialPolygon.getPoints().size();
    Set<Point> points = new HashSet<>();
    for (var startSegment : startSegments) {
      addPoints(startSegment, points);
    }
    if (points.size() != expectedSize) {
      log.warn("Total points covered={} while total points={}", points.size(), expectedSize);
    }
    for (var startSegment : startSegments) {
      checkRepeatedPoints(startSegment);
    }
    int maxFaceId = -1;
    for (var startSegment : startSegments) {
      maxFaceId = Integer.max(maxFaceId, startSegment.getFaceReferenceId());
    }
    if (maxFaceId != startSegments.size() + 1) {
      log.warn("Max face id is {} but number of segments is {}", maxFaceId, startSegments.size());
    }
    log.info("Finished checking for instance");
  }

  private static void addPoints(LineSegment start, Set<Point> points) {
    var curr = start;
    if (curr.getNext() == null) {
      log.error("Curr has no next {}", curr);
    }
    points.add(curr.getStart());
    do {
      points.add(curr.getEnd());
      curr = curr.getNext();
    } while (curr != start);
  }

  private static void checkRepeatedPoints(LineSegment start) {
    var curr = start;
    Set<Point> points = new HashSet<>();
    do {
      if (points.contains(curr.getEnd())) {
        log.warn("Point {} isHole={} gets repeated in face ID {}", curr.getEnd(), curr.getEnd().isHole(), start.getFaceReferenceId());
      }
      points.add(curr.getEnd());
      curr = curr.getNext();
    } while (curr != start);
  }
}
