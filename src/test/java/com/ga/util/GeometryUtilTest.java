package com.ga.util;

import com.ga.data.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeometryUtilTest {

  @Test
  public void testCollinear() {
    Point a = new Point(1,1);
    Point b = new Point(2,2);
    Point c = new Point(3,3);
    assertEquals(GeometryUtil.orientationTest(a, b, c), GeometryUtil.OrientationResult.COLLINEAR);
  }

  @Test
  public void testLeft() {
    Point a = new Point(0,0);
    Point b = new Point(20,20);
    Point c = new Point(4,6);
    assertEquals(GeometryUtil.orientationTest(a,b,c), GeometryUtil.OrientationResult.LEFT);
  }

  @Test
  public void testRight() {
    Point a = new Point(0,0);
    Point b = new Point(20,20);
    Point c = new Point(6,4);
    assertEquals(GeometryUtil.orientationTest(a,b,c), GeometryUtil.OrientationResult.RIGHT);
  }

}