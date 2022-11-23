package com.ga.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ga.data.ProblemInstance;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Reader {
  private static final ObjectMapper mapper = new ObjectMapper();

  @SneakyThrows
  public static List<ProblemInstance> getProblemInstances(Path instancesPath) {
    return Files.walk(instancesPath)
        .filter(path -> !path.toFile().isDirectory())
        .map(path -> path.toAbsolutePath().toString())
        .map(Reader::toProblemInstance)
        .limit(1)
        .toList();
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
