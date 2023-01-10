package com.ga.monotone;

import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.Point;
import com.ga.data.PrevNext;
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
        FaceReferenceGenerator.assignFaceId(segment);
        distinctSegments.add(segment);
      }
      if (segment.getSibling().getFaceReferenceId() == 0) {
        FaceReferenceGenerator.assignFaceId(segment.getSibling());
        distinctSegments.add(segment.getSibling());
      }
    }
    return distinctSegments;
  }

  public static LineSegment createSegment(PrevNext startSegments, PrevNext endSegments) {
    var start = startSegments.prev().getEnd();
    var end = endSegments.prev().getEnd();

    var segmentOne = new LineSegment(start, end);
    var segmentTwo = new LineSegment(end, start);

    //Set siblings
    segmentTwo.setSibling(segmentOne);
    segmentOne.setSibling(segmentTwo);

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

  public static LineSegment createSegment(Point start, Point end) {
    var startSegments = SegmentAdderUtil.getPrevNextFromViewer(start, end);
    var endSegments = SegmentAdderUtil.getPrevNextFromViewer(end, start);
    return createSegment(startSegments, endSegments);
  }

}
