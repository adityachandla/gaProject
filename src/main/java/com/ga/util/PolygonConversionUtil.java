package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class PolygonConversionUtil {

  public static List<List<Point>> getPolygonPoints(List<LineSegment> polygons) {
    return polygons.stream()
        .map(PolygonConversionUtil::getPoints)
        .filter(PolygonConversionUtil::hasPositiveArea)
        .filter(PolygonConversionUtil::hasUniquePoints)
        .toList();
  }

  private static boolean hasUniquePoints(List<Point> points) {
    var hs = new HashSet<Point>();
    for (var p : points) {
      if (hs.contains(p)) {
        log.info("Has duplicate points");
        return false;
      }
      hs.add(p);
    }
    return true;
  }

  private static boolean hasPositiveArea(List<Point> points) {
    long area = 0;
    for (int i = 0; i < points.size() - 1; i++) {
      var t = points.get(i);
      var n = points.get(i + 1);
      area += (t.getY() + n.getY()) * (t.getX() - n.getX());
    }
    var last = points.get(points.size() - 1);
    var first = points.get(0);
    area += (last.getY() + first.getY()) * (last.getX() - first.getX());
    if (area < 0)
      log.info("Got area {}", area);
    return area > 0;
  }

  private static List<Point> getPoints(LineSegment segment) {
    var list = new ArrayList<Point>();
    assert segment != null;
    var curr = segment;
    do {
      list.add(curr.getStart());
      if (curr.getNext() == null) {
        log.error("Curr is {}", curr);
      }
      curr = curr.getNext();
    } while (curr != segment);
    return list;
  }
}
