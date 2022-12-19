package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

@Slf4j
public class ConvexConverter {

  public static void convertToConvexPolygons(Polygon polygon) {
    addYMonotoneEdges(polygon);
  }

  private static void addYMonotoneEdges(Polygon polygon) {
    //Going from top to bottom and left to right
    var comparator = Comparator.<Point>comparingInt(p -> p.getY())
        .reversed()
        .thenComparingInt(p -> p.getX());
    Collections.sort(polygon.getPoints(), comparator);

    var leftLines = new TreeSet<LineSegment>(Comparator.comparingInt(line -> line.getStart().getX()));
    var helper = new HashMap<LineSegment, Point>();

    for (var point : polygon.getPoints()) {
      //TODO check how to identify points and add handlers
    }
  }
}
