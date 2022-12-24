package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.Polygon;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SegmentAdder {
  public static List<LineSegment> addLineSegments(List<LineSegment> toAdd) {
    Set<LineSegment> doubleSegments = new HashSet<>();
    for (var segmentOne : toAdd) {
      log.info("Adding segment {}", segmentOne);
      var startPoint = segmentOne.getStart();
      var endPoint = segmentOne.getEnd();
      var startType = GeometryUtil.getPointType(startPoint);
      var endType = GeometryUtil.getPointType(endPoint);
      if (endType == GeometryUtil.PointType.MERGE || startType == GeometryUtil.PointType.SPLIT) {
        segmentOne.swapPoints();
        startPoint = segmentOne.getStart();
        endPoint = segmentOne.getEnd();
      }
      doubleSegments.add(addSegment(startPoint, endPoint, segmentOne));
      doubleSegments.add(segmentOne);
    }
    return List.of();
  }

  private static void loopOver(LineSegment start) {
    var curr = start;
    do {
      curr = curr.getNext();
    } while(curr != start);
  }

  private static LineSegment addSegment(Point startPoint, Point endPoint, LineSegment segmentOne) {
    var startPointCopy = new Point(startPoint);
    var endPointCopy = new Point(endPoint);

    var startSegments = GeometryUtil.getPrevNext(startPoint);
    var endSegments = GeometryUtil.getPrevNext(endPoint);
    //Start to end is already added created, we'll also create end to start
    var segmentTwo = new LineSegment(endPointCopy, startPointCopy);
    segmentTwo.addReferenceToPoints();
    //Set siblings
    segmentTwo.setSibling(segmentOne);
    segmentOne.setSibling(segmentTwo);

    //Remove old segments
    startPoint.getSegments().remove(startSegments.next());
    endPoint.getSegments().remove(endSegments.prev());

    //Add new segments
    startPoint.getSegments().add(segmentOne);
    endPoint.getSegments().add(segmentOne);
    startPointCopy.getSegments().add(segmentTwo);
    startPointCopy.getSegments().add(startSegments.next());
    endPointCopy.getSegments().add(segmentTwo);
    endPointCopy.getSegments().add(endSegments.prev());

    //Add prev and next
    segmentOne.setPrev(startSegments.prev());
    segmentOne.setNext(endSegments.next());
    segmentTwo.setPrev(endSegments.prev());
    segmentTwo.setNext(startSegments.next());

    return segmentTwo;
  }
}
