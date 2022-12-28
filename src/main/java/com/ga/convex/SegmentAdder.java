package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.GeometryUtil;
import com.ga.util.PrevNext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.geometry.euclidean.twod.Ray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SegmentAdder {
  /**
   * This function updates the pointers to add segments and then loops over added
   * segments to deduplicate them.
   * @param toAdd List of segments to add
   * @return List of starting segments for all polygons
   */
  public static List<LineSegment> addLineSegments(List<LineSegment> toAdd) {
    List<LineSegment> doubleSegments = getDoubleSegments(toAdd);
    List<LineSegment> distinctPolygonSegments = new ArrayList<>();
    Set<Point> seenPoints = new HashSet<>();
    for (var segment : doubleSegments) {
      distinctPolygonSegments.add(segment);
      addFaceReferenceId(segment, seenPoints);
    }
    log.info("Total seen points {}", seenPoints.size());
    return distinctPolygonSegments;
  }

  private static void addFaceReferenceId(LineSegment segment, Set<Point> seenPoints) {
    var curr = segment;
    do {
      seenPoints.add(segment.getStart());
      seenPoints.add(segment.getEnd());
      curr.setFaceReferenceId(FaceReferenceGenerator.getAndIncrementReferenceId());
      curr = curr.getNext();
    } while (curr != segment);
  }

  private static List<LineSegment> getDoubleSegments(List<LineSegment> segments) {
    List<LineSegment> doubleSegments = new ArrayList<>();
    for (var segment : segments) {
      var startPoint = segment.getStart();
      var endPoint = segment.getEnd();
      var addedSegment = createSegment(startPoint, endPoint);
      doubleSegments.add(addedSegment);
      doubleSegments.add(addedSegment.getSibling());
    }
    return doubleSegments;
  }

  public static LineSegment createSegment(Point start, Point end) {
    var segmentOne = new LineSegment(start, end);
    var segmentTwo = new LineSegment(end, start);

    //Reference to points
    segmentOne.addReferenceToPoints();
    segmentTwo.addReferenceToPoints();

    //Set siblings
    segmentTwo.setSibling(segmentOne);
    segmentOne.setSibling(segmentTwo);

    var startSegments = GeometryUtil.getPrevNext(start);
    var endSegments = GeometryUtil.getPrevNext(end);

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

  private static PrevNext getPrevNext(Point point) {
    var prevNextPairs = getPrevNextPairs(point);
    if (prevNextPairs.size() == 1) {
      return prevNextPairs.get(0);
    }
    return SegmentAdderUtil.getFirstPairClockwise(prevNextPairs, point);
  }

  private static List<PrevNext> getPrevNextPairs(Point p) {
    var pairs = new ArrayList<PrevNext>();
    for (var one : p.getSegments()) {
      for (var two : p.getSegments()) {
        if (one.getNext() == two) {
          pairs.add(new PrevNext(one, two));
        }
      }
    }
    return pairs;
  }
}
