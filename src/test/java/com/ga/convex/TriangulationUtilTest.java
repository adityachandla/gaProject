package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.triangulation.TriangulationUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TriangulationUtilTest {

  @Test
  public void testBoundary() {
    var polygon = getPolygon();
    List<LineSegment> segments = TriangulationUtil.triangulateYMonotone(polygon);
    System.out.println(segments);
    for (var segment: segments) {
      assertEquals(segment.getNext().getNext().getNext(), segment);
    }
  }

  private LineSegment getPolygon() {
    var p1 = new Point(84, 176);
    var p2 = new Point(144, 176);
    var p3 = new Point(174, 161);
    var p4 = new Point(177, 131);
    var p5 = new Point(146, 109);
    var p6 = new Point(92, 109);
    var p7 = new Point(54, 128);
    var one = new LineSegment(p2,p1);
    one.addReferenceToPoints();
    var two = new LineSegment(p3,p2);
    two.addReferenceToPoints();
    var three = new LineSegment(p4,p3);
    three.addReferenceToPoints();
    var four = new LineSegment(p5,p4);
    four.addReferenceToPoints();
    var five = new LineSegment(p6,p5);
    five.addReferenceToPoints();
    var six = new LineSegment(p7,p6);
    six.addReferenceToPoints();
    var seven = new LineSegment(p1,p7);
    seven.addReferenceToPoints();
    one.setPrev(two);
    two.setPrev(three);
    three.setPrev(four);
    four.setPrev(five);
    five.setPrev(six);
    six.setPrev(seven);
    seven.setPrev(one);

    one.setNext(seven);
    two.setNext(one);
    three.setNext(two);
    four.setNext(three);
    five.setNext(four);
    six.setNext(five);
    seven.setNext(six);
    return six;
  }


}