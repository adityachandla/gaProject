package com.ga.util;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.ProblemInstance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolygonCreationUtilTest {

  @Test
  public void createPolygonTest() {
    var instance = getProblemInstance();
    var polygon = PolygonCreationUtil.createPolygon(instance);
    int totalPoints = instance.outerBoundary().size()+instance.holes().get(0).size();
    assertEquals(polygon.getPoints().size(), totalPoints);
    assertEquals(polygon.getHoles().size(), 1);
    verifyGoingForwards(polygon.getStartingSegment());
    verifyGoingBackwards(polygon.getStartingSegment());
    for (var hole: polygon.getHoles()) {
      verifyGoingForwards(hole);
      verifyGoingBackwards(hole);
    }
  }

  private void verifyGoingForwards(LineSegment start) {
    var curr = start.getNext();
    while (curr != start) {
      assertNotNull(curr.getStart());
      assertNotNull(curr.getEnd());
      curr = curr.getNext();
    }
  }

  private void verifyGoingBackwards(LineSegment start) {
    var curr = start.getPrev();
    while (curr != start) {
      assertNotNull(curr.getStart());
      assertNotNull(curr.getEnd());
      curr = curr.getPrev();
    }
  }

  private ProblemInstance getProblemInstance() {
    String name = "dummyProblem";
    var points = List.of(new Point(100,100), new Point(20, 10), new Point(50, 50), new Point(60,60));
    var holeOne = List.of(new Point(20,20), new Point(30, 30), new Point(20, 30));
    for (var hole: holeOne) {
      hole.setHole(true);
    }
    return new ProblemInstance(name, points, List.of(holeOne));
  }

}