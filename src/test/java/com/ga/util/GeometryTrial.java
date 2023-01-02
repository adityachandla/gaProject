package com.ga.util;

import com.ga.data.Point;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Test;

public class GeometryTrial {

  private static final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-10);

  @Test
  public void testAngle() {
    var lineOne = Lines.fromPoints(Vector2D.of(20, 30), Vector2D.of(40,50), precision);
    var lineTwo = Lines.fromPoints(Vector2D.of(40,50), Vector2D.of(20, 11), precision);
    System.out.println(lineOne.angle(lineTwo));
    System.out.println(lineTwo.angle(lineOne));
    System.out.println(lineOne.getDirection().getX());
    System.out.println(precision.gte(lineOne.getDirection().getY(), 0)); //Gives true when Y >= 0
    System.out.println(lineTwo.getDirection().getX());
    System.out.println(lineTwo.getDirection().getY());
  }

  @Test
  public void testAngleTwo() {
    var a = Vector2D.of(52,57);
    var ac = Lines.fromPoints(a, Vector2D.of(61,33), precision);
    var ab = Lines.fromPoints(a, Vector2D.of(43,31), precision);
    var ad = Lines.fromPoints(a, Vector2D.of(52,83), precision);
    System.out.println(getAngle(ac,ab));
    System.out.println(getAngle(ac,ad));
  }


  private double getAngle(Line one, Line two) {
    double angle = one.angle(two);
    if (angle < 0) return angle+(2*Math.PI);
    return angle;
  }

  @Test
  public void testClockwise() {
    var p1 = Vector2D.of(55,55);
    var p2 = Vector2D.of(38,57);
    var dirOne = Lines.fromPoints(p1,p2, precision);
    System.out.println(dirOne);
    var p3 = Vector2D.of(44,66);
    var dirTwo = Lines.fromPoints(p1,p3, precision);
    System.out.println(dirTwo);
    var p4 = Vector2D.of(34, 71);
    var dirThree = Lines.fromPoints(p1,p4, precision);;
    System.out.println(dirThree);
    System.out.println(dirOne.angle(dirTwo));
    System.out.println(dirTwo.angle(dirThree));
  }

  @Test
  public void testDirections() {
    var p1 = Vector2D.of(10, 10);
    var p2 = Vector2D.of(10, 5);
    var p3 = Vector2D.of(5, 10);
    var l1 = Lines.fromPoints(p1, p2, precision);
    var l2 = Lines.fromPoints(p1, p3, precision);
    System.out.println(l1.angle(l2));
  }
}
