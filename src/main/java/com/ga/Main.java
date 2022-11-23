package com.ga;

import com.ga.io.Reader;
import com.ga.util.PolygonCreationUtil;
import com.ga.util.TriangulationUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Provide path to instances");
      System.exit(1);
    }
    log.info("Got arguments: {}", Arrays.toString(args));
    //TODO read one by one so that gc can kick in cause there's a shitload of points
    var problemInstances = Reader.getProblemInstances(Path.of(args[0]));
    log.info("Total instances {}", problemInstances.size());
    for (var problemInstance: problemInstances) {
      log.info("Processing instance with {} boundary points and {} holes",
          problemInstance.outerBoundary().size(),
          problemInstance.holes().size());
      var polygon = PolygonCreationUtil.createPolygon(problemInstance);
      log.info("Created polygon with total points {}", polygon.getPoints().size());
      TriangulationUtil.triangulatePolygon(polygon);
    }
  }

}
