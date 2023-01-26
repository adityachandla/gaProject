package com.ga.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ga.data.Point;
import com.ga.data.ProblemOutput;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Slf4j
public class Writer {
  @SneakyThrows(IOException.class)
  public static void writeSolution(String basePath, String instanceName, List<List<Point>> polygonPoints) {
    var outputPath = String.format("%s/%s.output.json", basePath, instanceName);
    var file = new File(outputPath);
    if (!file.exists()) {
      if (!file.createNewFile()) {
        throw new RuntimeException("Unable to create file");
      }
    }
    var output = new ProblemOutput("CGSHOP2023_Solution", instanceName, polygonPoints);
    var mapper = new ObjectMapper();
    var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    writer.write(mapper.writeValueAsString(output));
    writer.close();
  }
}
