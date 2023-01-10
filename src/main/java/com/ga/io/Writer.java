package com.ga.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.ProblemOutput;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

@Slf4j
public class Writer {
  @SneakyThrows(IOException.class)
  public static void writeSolution(String basePath, String instanceName, List<LineSegment> polygons) {
    var outputPath = String.format("%s/%s.output.json", basePath, instanceName);
    var file = new File(outputPath);
    if (!file.exists()) {
      if(!file.createNewFile()) {
        throw new RuntimeException("Unable to create file");
      }
    }
    var output = new ProblemOutput("CGSHOP2023_Solution", instanceName, getPolygonPoints(polygons));
    var mapper = new ObjectMapper();
    var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    writer.write(mapper.writeValueAsString(output));
    writer.close();
  }

  private static List<List<Point>> getPolygonPoints(List<LineSegment> polygons) {
    return polygons.stream()
        .map(Writer::getPoints)
        .filter(Writer::hasPositiveArea)
        .filter(Writer::hasUniquePoints)
        .toList();
  }

  private static boolean hasUniquePoints(List<Point> points) {
    var hs = new HashSet<Point>();
    for (var p :points) {
      if(hs.contains(p)) {
        return false;
      }
      hs.add(p);
    }
    return true;
  }

  private static boolean hasPositiveArea(List<Point> points) {
    long area = 0;
    for(int i = 0; i < points.size()-1; i++) {
      var t = points.get(i);
      var n = points.get(i+1);
      area += (t.getY() + n.getY())*(t.getX()-n.getX());
    }
    var last = points.get(points.size()-1);
    var first = points.get(0);
    area += (last.getY() + first.getY())*(last.getX()-first.getX());
    if (area < 0) 
      log.info("Got area {}", area);
    return area > 0;
  }

  private static List<Point> getPoints(LineSegment segment) {
    var list = new ArrayList<Point>();
    var curr = segment;
    do {
      list.add(curr.getStart());
      curr = curr.getNext();
    } while (curr != segment);
    return list;
  }
}
