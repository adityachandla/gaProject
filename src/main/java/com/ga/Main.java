package com.ga;

import com.ga.convex.ConvexConverter;
import com.ga.data.FaceReferenceGenerator;
import com.ga.io.Reader;
import com.ga.io.Writer;
import com.ga.util.PolygonConversionUtil;
import com.ga.util.PolygonCreationUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
public class Main {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Provide path to instances and path for output");
      System.exit(1);
    }
    log.info("Got arguments: {}", Arrays.toString(args));
    long startTime = System.currentTimeMillis();
    var problemInstances = Reader.getProblemInstances(Path.of(args[0]));
    log.info("Total instances {}", problemInstances.size());
    for (var problemInstance : problemInstances) {
      FaceReferenceGenerator.clear();
      log.info("Processing instance {} with {} boundary points and {} holes",
          problemInstance.name(),
          problemInstance.outerBoundary().size(),
          problemInstance.holes().size());
      var polygon = PolygonCreationUtil.createPolygon(problemInstance);
      log.info("Created polygon with total points {}", polygon.getPoints().size());
      var convexPolygons = ConvexConverter.convertToConvexPolygons(polygon);
      var polygonPoints = PolygonConversionUtil.getPolygonPoints(convexPolygons);
      Writer.writeSolution(args[1], problemInstance.name(), polygonPoints);
    }
    log.info("Total running time: {}", System.currentTimeMillis() - startTime);
  }


}
