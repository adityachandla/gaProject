package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SegmentAdder {
  public static List<LineSegment> addLineSegments(List<LineSegment> toAdd) {
    Set<LineSegment> doubleSegments = getDoubleSegments(toAdd);
    Set<LineSegment> seenDoubleSegments = new HashSet<>();
    List<LineSegment> distinctPolygonSegments = new ArrayList<>();
    for (var segment : doubleSegments) {
      if (seenDoubleSegments.contains(segment)) {
        continue;
      }
      distinctPolygonSegments.add(segment);
      loopOver(segment, seenDoubleSegments);
    }
    return distinctPolygonSegments;
  }

  private static void loopOver(LineSegment segment, Set<LineSegment> seen) {
    var curr = segment;
    do {
      if (curr.getSibling() != null) {
        seen.add(curr);
      }
      curr = curr.getNext();
    } while (curr != segment);
  }

  private static Set<LineSegment> getDoubleSegments(List<LineSegment> segments) {
    Set<LineSegment> doubleSegments = new HashSet<>();
    for (var segment : segments) {
      var startPoint = segment.getStart();
      var endPoint = segment.getEnd();
      var addedSegment = createSegment(startPoint, endPoint);
      doubleSegments.add(addedSegment);
      assert addedSegment.getSibling() != null;
      doubleSegments.add(addedSegment.getSibling());
    }
    return doubleSegments;
  }

  /**
   * Life Lesson: This is what happens when everything points to everything
   *
   * @param start Start point of the segment
   * @param end end point of the segment
   * @return One of the two line segments added. We can get the other using getSibling.
   */
  public static LineSegment createSegment(Point start, Point end) {
    var startCopy = new Point(start);
    var endCopy = new Point(end);

    var segmentOne = new LineSegment(start, endCopy);
    var segmentTwo = new LineSegment(end, startCopy);

    //Set siblings
    segmentTwo.setSibling(segmentOne);
    segmentOne.setSibling(segmentTwo);

    var startSegments = GeometryUtil.getPrevNext(start);
    var endSegments = GeometryUtil.getPrevNext(end);
    //Remove old segments
    start.getSegments().remove(startSegments.next());
    end.getSegments().remove(endSegments.next());

    //Add new segments
    start.getSegments().add(segmentOne);
    end.getSegments().add(segmentTwo);
    startCopy.getSegments().add(segmentTwo);
    startCopy.getSegments().add(startSegments.next());
    endCopy.getSegments().add(segmentOne);
    endCopy.getSegments().add(endSegments.next());

    //Add prev and next to new segments
    segmentOne.setPrev(startSegments.prev());
    segmentOne.setNext(endSegments.next());
    segmentTwo.setPrev(endSegments.prev());
    segmentTwo.setNext(startSegments.next());

    //Update prev and next for old segments
    startSegments.prev().setNext(segmentOne);
    endSegments.next().setPrev(segmentOne);
    startSegments.next().setPrev(segmentTwo);
    endSegments.prev().setNext(segmentTwo);

    return segmentTwo;
  }
}
