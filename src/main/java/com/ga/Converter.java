package com.ga;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * This file is solely to convert geogebra xml to input format and should
 * not be used anywhere in the codebase.
 */
@UtilityClass
public class Converter {

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.out.println("Enter the Geo-gebra file to convert and output file location");
      System.exit(1);
    }

    var fileInput = readFile(args[0]);
    List<Polygon> polygons = getPolygons(fileInput.commands());
    Map<String, PointElement> points = parsePoints(fileInput.elements());
    markHoles(polygons, fileInput.elements());
    var input = convertToProblemInput(points, polygons);
    writeToFile(input, args[1]);
  }

  private static void writeToFile(InputFormat input, String filePath) throws IOException {
    var mapper = new ObjectMapper();
    var outputString = mapper.writeValueAsString(input);
    var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
    writer.write(outputString);
    writer.close();
  }

  private static InputFormat convertToProblemInput(Map<String, PointElement> points, List<Polygon> polygons) {
    var boundaryPolygon = polygons.stream()
        .filter(Predicate.not(Polygon::isHole))
        .map(polygon -> getBoundary(points, polygon))
        .findAny()
        .orElseThrow();
    var holes = polygons.stream()
        .filter(Polygon::isHole)
        .map(polygon -> getBoundary(points, polygon))
        .toList();
    return new InputFormat("testInput", boundaryPolygon, holes);
  }

  private static List<PointElement> getBoundary(Map<String, PointElement> points, Polygon polygon) {
    return polygon.getPointLabels().stream()
        .map(points::get)
        .toList();
  }

  private static void markHoles(List<Polygon> polygons, List<String> elements) throws IOException {
    Set<String> holeTags = new HashSet<>();
    var mapper = new XmlMapper();
    for (var element : elements) {
      if (!element.contains("type=\"polygon\"")) continue;
      var polygonElement = mapper.readValue(element, PolygonElement.class);
      if (polygonElement.getObjColor().getR() >= 200) {
        holeTags.add(polygonElement.getLabel());
      }
    }
    for (var polygon : polygons) {
      if (holeTags.contains(polygon.getTag())) {
        polygon.setHole(true);
      }
    }
  }

  private static Map<String, PointElement> parsePoints(List<String> elements) throws IOException {
    Map<String, PointElement> elementMap = new HashMap<>();
    var mapper = new XmlMapper();
    for (var element : elements) {
      if (notAPoint(element)) {
        continue;
      }
      var ele = mapper.readValue(element, Element.class);
      var pointElement = new PointElement((int) ele.getCoords().getX(), (int) ele.getCoords().getY());
      elementMap.put(ele.getLabel(), pointElement);
    }
    return elementMap;
  }

  private static boolean notAPoint(String element) {
    return !element.contains("type=\"point\"");
  }

  private static List<Polygon> getPolygons(List<String> commands) {
    var labelPattern = Pattern.compile("<output a0=\"(.*?)\"");
    var pointLabelPattern = Pattern.compile("\\w\\d=\"(.*?)\"");
    var polygonList = new ArrayList<Polygon>();
    for (String command : commands) {
      var polygon = new Polygon();
      var labelMatcher = labelPattern.matcher(command);
      labelMatcher.find();
      polygon.setTag(labelMatcher.group(1));
      command = command.replaceAll("<output.*?/>", "");

      var pointLabelMatcher = pointLabelPattern.matcher(command);
      List<String> pointLabels = new ArrayList<>();
      while (pointLabelMatcher.find()) {
        pointLabels.add(pointLabelMatcher.group(1));
      }
      polygon.setPointLabels(pointLabels);
      polygonList.add(polygon);
    }
    return polygonList;
  }

  @SneakyThrows(IOException.class)
  private static FileInput readFile(String filePath) {
    var br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
    List<String> elements = readTag("element", br);
    br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
    List<String> commands = readTag("command", br);
    return new FileInput(elements, commands);
  }

  private static List<String> readTag(String tag, BufferedReader br) throws IOException {
    List<String> list = new ArrayList<>();
    boolean inElement = false;
    var sb = new StringBuilder();
    String s;
    while ((s = br.readLine()) != null) {
      if (inElement) {
        sb.append(s);
      }
      if (s.startsWith("<" + tag)) {
        sb.append(s);
        inElement = true;
      }
      if (s.startsWith("</" + tag)) {
        inElement = false;
        list.add(sb.toString());
        sb = new StringBuilder();
      }
    }
    return list;
  }

  private record FileInput(List<String> elements, List<String> commands) {
  }

  private record InputFormat(
      String name,
      @JsonProperty("outer_boundary")
      List<PointElement> outerBoundary,
      List<List<PointElement>> holes
  ) {
  }


  @Data
  private static class Polygon {
    private String tag;
    private List<String> pointLabels;
    private boolean isHole;
  }

  @Data
  @AllArgsConstructor
  private static class PointElement {
    private int x;
    private int y;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Element {
    @JacksonXmlProperty(isAttribute = true)
    private String label;
    private Coords coords;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Coords {
      private double x;
      private double y;
    }
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class PolygonElement {
    @JacksonXmlProperty(isAttribute = true)
    private String label;
    private Color objColor;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Color {
      private int r, g, b;
    }
  }
}
