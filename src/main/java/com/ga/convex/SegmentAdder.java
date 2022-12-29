package com.ga.convex;

import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.util.PrevNext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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
    for (var segment : segments) {
      if (segment.getFaceReferenceId() == 0) {
        addFaceReferenceId(segment);
        distinctSegments.add(segment);
      }
      if (segment.getSibling().getFaceReferenceId() == 0) {
        addFaceReferenceId(segment.getSibling());
        distinctSegments.add(segment.getSibling());
      }
    }
    return distinctSegments;
  }

  private static void addFaceReferenceId(LineSegment segment) {
    var curr = segment;
    int referenceId = FaceReferenceGenerator.getAndIncrementReferenceId();
    do {
      curr.setFaceReferenceId(referenceId);
      curr = curr.getNext();
    } while (curr != segment);
  }

  public static LineSegment createSegment(Point start, Point end) {
    assert start.getY() <= end.getY();
    var segmentOne = new LineSegment(start, end);
    var segmentTwo = new LineSegment(end, start);

    //Set siblings
    segmentTwo.setSibling(segmentOne);
    segmentOne.setSibling(segmentTwo);

    var startSegments = SegmentAdderUtil.getPrevNextFromViewer3(start, end);
    var endSegments = SegmentAdderUtil.getPrevNextFromViewer3(end, start);

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

  private static PrevNext getLatestPrevNext(Point p) {
    var segments = p.getSegments();
    var prev = segments.get(segments.size() - 1);
    var next = segments.get(segments.size() - 2);
    if (prev.getNext() != next) {
      return new PrevNext(next, prev);
    }
    return new PrevNext(prev, next);
  }


}
