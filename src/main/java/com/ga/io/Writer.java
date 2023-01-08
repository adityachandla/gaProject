package com.ga.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.ProblemOutput;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class Writer {
  @SneakyThrows(IOException.class)
  public static void writeSolution(String basePath, String instanceName, List<LineSegment> polygons) {
    var outputPath = String.format("%s/%s.output.json", basePath.toString(), instanceName);
    var file = new File(outputPath);
    if (!file.exists()) {
      file.createNewFile();
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
        .toList();
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
