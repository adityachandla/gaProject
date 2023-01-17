package com.ga.convex;

import com.ga.data.FaceReferenceGenerator;
import com.ga.data.LineSegment;
import com.ga.data.PrevNext;
import com.ga.util.GeometryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class PolygonMerger {

  public static List<LineSegment> mergePolygons(List<LineSegment> addedSegments) {
    var removableSegment = getRemovableSegment(addedSegments);
    while (removableSegment != null) {
      addedSegments.remove(removableSegment);
      addedSegments.remove(removableSegment.getSibling());
      var next = removableSegment.getNext();
      addedSegments.remove(next);
      addedSegments.add(next);
      FaceReferenceGenerator.assignFaceId(next);
      var edgeRemover = new EdgeRemover(removableSegment);
      edgeRemover.softDelete();
      edgeRemover.hardDelete();
      removableSegment = getRemovableSegment(addedSegments);
    }
    var uniqueSegments = new ArrayList<LineSegment>();
    var faceReferenceIds = new HashSet<Integer>();
    for (var segment: addedSegments) {
      if (!faceReferenceIds.contains(segment.getFaceReferenceId())) {
        faceReferenceIds.add(segment.getFaceReferenceId());
        uniqueSegments.add(segment);
      }
    }
    return uniqueSegments;
  }

  private static LineSegment getRemovableSegment(final List<LineSegment> addedSegments) {
    for (var segment : addedSegments) {
      if (segment.getSibling() == null) continue;
      int sizeOfFirst = sizeOfPolygon(segment);
      int sizeOfSecond = sizeOfPolygon(segment.getSibling());
      var edgeRemover = new EdgeRemover(segment);
      var next = segment.getNext();
      edgeRemover.softDelete();
      int sizeAfterRemoval = sizeOfPolygon(next);
      if (GeometryUtil.isConvex(next) && (sizeOfFirst + sizeOfSecond - 2) == sizeAfterRemoval) {
        edgeRemover.revertSoftDelete();
        return segment;
      }
      edgeRemover.revertSoftDelete();
    }
    return null;
  }

  private static int sizeOfPolygon(LineSegment segment) {
    var curr = segment;
    int count = 0;
    if (curr.getNext() == null) {
      log.info("curr is {}", curr);
    }
    do {
      curr = curr.getNext();
      count++;
    } while (curr != segment);
    return count;
  }

  private static class EdgeRemover {
    private LineSegment segment;
    private LineSegment sibling;
    private PrevNext segmentPrevNext;
    private PrevNext siblingPrevNext;

    EdgeRemover(LineSegment segment) {
      this.segment = segment;
      assert segment.getSibling() != null;
      this.sibling = segment.getSibling();
    }

    public void softDelete() {
      assert segmentPrevNext == null;
      assert siblingPrevNext == null;
      segmentPrevNext = new PrevNext(segment.getPrev(), segment.getNext());
      siblingPrevNext = new PrevNext(sibling.getPrev(), sibling.getNext());

      if (segment.getPrev() == null) {
        log.warn("Prev is null for {}", segment);
      }
      segment.getPrev().setNext(siblingPrevNext.next());
      segment.getNext().setPrev(siblingPrevNext.prev());

      sibling.getNext().setPrev(segmentPrevNext.prev());
      sibling.getPrev().setNext(segmentPrevNext.next());
      assert segmentPrevNext.prev().getNext() == siblingPrevNext.next();
      assert siblingPrevNext.prev().getNext() == segmentPrevNext.next();

      assert segmentPrevNext.next().getPrev() == siblingPrevNext.prev();
      assert siblingPrevNext.next().getPrev() == segmentPrevNext.prev();
    }

    public void hardDelete() {
      segment.removeReferenceFromPoints();
      sibling.removeReferenceFromPoints();
      segment.setNext(null);
      segment.setFaceReferenceId(-1);
      segment.setPrev(null);
      sibling.setPrev(null);
      sibling.setFaceReferenceId(-1);
      sibling.setNext(null);
    }

    public void revertSoftDelete() {
      assert segmentPrevNext != null;
      assert siblingPrevNext != null;
      segmentPrevNext.next().setPrev(segment);
      segmentPrevNext.prev().setNext(segment);

      siblingPrevNext.next().setPrev(sibling);
      siblingPrevNext.prev().setNext(sibling);
    }
  }
}
