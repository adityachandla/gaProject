package com.ga.util;

import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Test;

public class GeometryTrial {

  @Test
  public void testAngle() {
    var precision = Precision.doubleEquivalenceOfEpsilon(1e-10);
    var lineOne = Lines.fromPoints(Vector2D.of(20, 30), Vector2D.of(40,50), precision);
    var lineTwo = Lines.fromPoints(Vector2D.of(40,50), Vector2D.of(20, 11), precision);
    System.out.println(lineOne.angle(lineTwo));
    System.out.println(lineTwo.angle(lineOne));
    System.out.println(lineOne.getDirection().getX());
    System.out.println(lineOne.getDirection().getY());
    System.out.println(lineTwo.getDirection().getX());
    System.out.println(lineTwo.getDirection().getY());
  }
}
