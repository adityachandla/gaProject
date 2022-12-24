package com.ga.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ga.data.Point;
import com.ga.data.ProblemInstance;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Reader {
  private static final ObjectMapper mapper = new ObjectMapper();

  @SneakyThrows
  public static List<ProblemInstance> getProblemInstances(Path instancesPath) {
    if (instancesPath.toFile().isFile()) {
      var instance = toProblemInstance(instancesPath.toAbsolutePath().toString());
      setHoles(instance);
      return List.of(instance);
    }
    return Files.walk(instancesPath)
        .filter(path -> !path.toFile().isDirectory())
        .map(path -> path.toAbsolutePath().toString())
        .map(Reader::toProblemInstance)
        .map(Reader::setHoles)
        .toList();
  }

  /**
   * This method is used to set the boolean flag of holes to true
   * @param instance Problem Instance
   * @return returns the same instance with correct values of holes
   */
  private static ProblemInstance setHoles(ProblemInstance instance) {
    instance.holes().stream()
        .flatMap(List::stream)
        .forEach(p -> p.setHole(true));
    return instance;
  }

  private static ProblemInstance toProblemInstance(String filePath) {
    try (var br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
      return mapper.readValue(br.readLine(), ProblemInstance.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Unable to read instance");
    }
  }
}
