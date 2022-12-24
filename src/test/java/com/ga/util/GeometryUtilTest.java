package com.ga.util;

import com.ga.data.LineSegment;
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

  @Test
  public void testEnd() {
    Point a = new Point(24, 42);
    Point b = new Point(40, 21);
    Point c = new Point(61, 44);
    var segOne = new LineSegment(a,b);
    segOne.addReferenceToPoints();
    var segTwo = new LineSegment(b,c);
    segTwo.addReferenceToPoints();
    segOne.setNext(segTwo);
    segTwo.setPrev(segOne);
    assertEquals(GeometryUtil.getPointType(b), GeometryUtil.PointType.END);
  }

  @Test
  public void testMerge() {
    Point a = new Point(24, 42);
    Point b = new Point(40, 21);
    Point c = new Point(61, 44);
    var segOne = new LineSegment(c,b);
    segOne.addReferenceToPoints();
    var segTwo = new LineSegment(b,a);
    segTwo.addReferenceToPoints();
    segOne.setNext(segTwo);
    segTwo.setPrev(segOne);
    assertEquals(GeometryUtil.getPointType(b), GeometryUtil.PointType.MERGE);
  }

  @Test
  public void testStart() {
    Point a = new Point(19, 21);
    Point b = new Point(39, 52);
    Point c = new Point(61, 25);
    var segOne = new LineSegment(c,b);
    segOne.addReferenceToPoints();
    var segTwo = new LineSegment(b,a);
    segTwo.addReferenceToPoints();
    segOne.setNext(segTwo);
    segTwo.setPrev(segOne);
    assertEquals(GeometryUtil.getPointType(b), GeometryUtil.PointType.START);
  }

  @Test
  public void testSplit() {
    Point a = new Point(19, 21);
    Point b = new Point(39, 52);
    Point c = new Point(61, 25);
    var segOne = new LineSegment(a,b);
    segOne.addReferenceToPoints();
    var segTwo = new LineSegment(b,c);
    segTwo.addReferenceToPoints();
    segOne.setNext(segTwo);
    segTwo.setPrev(segOne);
    assertEquals(GeometryUtil.getPointType(b), GeometryUtil.PointType.SPLIT);
  }

  @Test
  public void testSameY() {
    var a = new Point(13,13);
    var b = new Point(13,14);
    var c = new Point(15,14);
    var segOne = new LineSegment(a,b);
    var segTwo = new LineSegment(b,c);
    segOne.addReferenceToPoints();
    segTwo.addReferenceToPoints();
    segOne.setNext(segTwo);
    segTwo.setPrev(segOne);
    assertEquals(GeometryUtil.getPointType(b), GeometryUtil.PointType.SPLIT);
  }

}