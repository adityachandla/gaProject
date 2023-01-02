package com.ga.monotone;

import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SegmentAdder {
  /**
   * This function updates the pointers to add segments and then loops over added
   * segments to deduplicate them.
   *
   * @param segments List of segments to add
   * @return List of starting segments for all polygons
   */
  public static List<LineSegment> addLineSegments(List<LineSegment> segments) {
    segments = segments.stream().map(s -> createSegment(s.getStart(), s.getEnd())).toList();
    List<LineSegment> distinctSegments = new ArrayList<>();
    Set<Point> allPoints = new HashSet<>();
    for (var segment : segments) {
      if (segment.getFaceReferenceId() == 0) {
        addFaceReferenceId(segment, allPoints);
        distinctSegments.add(segment);
      }
      if (segment.getSibling().getFaceReferenceId() == 0) {
        addFaceReferenceId(segment.getSibling(), allPoints);
        distinctSegments.add(segment.getSibling());
      }
    }
    log.info("Total points {}", allPoints.size());
    return distinctSegments;
  }

  private static void addFaceReferenceId(LineSegment segment, Set<Point> allPoints) {
    var curr = segment;
    int referenceId = FaceReferenceGenerator.getAndIncrementReferenceId();
    Set<Point> seenPoints = new HashSet<>();
    allPoints.add(curr.getStart());
    do {
      var end = curr.getEnd();
      if(seenPoints.contains(end)) {
        log.error("Point revisited {} isHole={} prevIsHole={}", end, end.isHole(), curr.getPrev().getEnd().isHole());
      }
      seenPoints.add(end);
      allPoints.add(end);
      curr.setFaceReferenceId(referenceId);
      curr = curr.getNext();
    } while (curr != segment);
  }

  public static LineSegment createSegment(Point start, Point end) {
    var segmentOne = new LineSegment(start, end);
    var segmentTwo = new LineSegment(end, start);

    //Set siblings
    segmentTwo.setSibling(segmentOne);
    segmentOne.setSibling(segmentTwo);

    var startSegments = SegmentAdderUtil.getPrevNextFromViewer(start, end);
    var endSegments = SegmentAdderUtil.getPrevNextFromViewer(end, start);

    segmentOne.addReferenceToPoints();
    segmentTwo.addReferenceToPoints();

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
