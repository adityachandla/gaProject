package com.ga.triangulation;

import com.ga.data.BoundaryPoint;
import com.ga.data.Point;
import com.ga.util.PrevNext;
import com.ga.data.BoundarySegment;
import com.ga.data.LineSegment;
import com.ga.monotone.SegmentAdder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Map;

@Slf4j
public class TriangulationEdgeGenerator {

  private final List<BoundaryPoint> boundaryPoints;
  private Stack<BoundaryPoint> stack;
  private List<LineSegment> addedSegments;
  private Map<Point, PrevNext> prevNextMap;

  public TriangulationEdgeGenerator(List<BoundaryPoint> boundaryPoints, Map<Point, PrevNext> prevNextMap) {
    this.boundaryPoints = boundaryPoints;
    this.prevNextMap = prevNextMap;
  }

  private void initializeStack() {
    stack = new Stack<>();
    stack.add(boundaryPoints.get(0));
    stack.add(boundaryPoints.get(1));
  }

  public List<LineSegment> getAddedEdges() {
    addedSegments = new ArrayList<>(boundaryPoints.size());
    initializeStack();
    for (int i = 2; i < boundaryPoints.size() - 1; i++) {
      var v = boundaryPoints.get(i);
      if (isTopOnDifferentBoundary(v)) {
        processDifferentBoundaries(v);
      } else {
        processSameBoundaries(v);
      }
    }
    processLastPoint();
    return addedSegments;
  }

  private boolean isTopOnDifferentBoundary(BoundaryPoint p) {
    var top = stack.peek();
    return p.isLeftBoundary() ^ top.isLeftBoundary();
  }

  private void processDifferentBoundaries(BoundaryPoint p) {
    //In order to maintain prev-next, we should always add an
    //edge that clips away a triangle. That is why we are adding
    //segments in the reverse order here.
    var toAdd = new ArrayList<BoundaryPoint>();
    while (stack.size() > 1) {
      var toProcess = stack.pop();
      toAdd.add(toProcess);
    }
    for(int i = toAdd.size()-1; i >= 0; i--) {
      addSegment(p, toAdd.get(i));
    }
    stack.pop();
    stack.push(toAdd.get(0));
    stack.push(p);
  }

  private void processSameBoundaries(BoundaryPoint p) {
    var popped = stack.pop();
    var prev = popped;
    var boundary = new BoundarySegment(p, popped);
    while (!stack.empty() && boundary.isVisible(stack.peek())) {
      var toProcess = stack.pop();
      prev = toProcess;
      addSegment(p, toProcess);
    }
    stack.push(prev);
    stack.push(p);
  }

  private void processLastPoint() {
    var last = boundaryPoints.get(boundaryPoints.size() - 1);
    if (!stack.empty()) stack.pop();
    while (stack.size() > 1) {
      var toProcess = stack.pop();
      addSegment(last, toProcess);
    }
  }

  private void addSegment(BoundaryPoint one, BoundaryPoint two) {
    PrevNext prevNextOne = prevNextMap.get(one.point());
    PrevNext prevNextTwo = prevNextMap.get(two.point());
    var createdSegment = SegmentAdder.createSegment(prevNextOne, prevNextTwo);
    addedSegments.add(createdSegment);
    if (one.isLeftBoundary() && two.isLeftBoundary()) {
      //If both on the left boundary, we need the edge from top to bottom
      addFromTopToBottom(one, two, createdSegment);
    } else if (!one.isLeftBoundary() && !two.isLeftBoundary()) {
      //If both on right boundary, we need the edge from bottom to top
      addFromBottomToTop(one, two, createdSegment);
    } else {
      //If the points are on different boundaries, we need the edge from right boundary to left
      addFromRightToLeft(one, two, createdSegment);
    }
  }

  private void addFromTopToBottom(BoundaryPoint top, BoundaryPoint bottom, LineSegment createdSegment) {
    if (top.point().getY() < bottom.point().getY()) {
      var temp = top;
      top = bottom;
      bottom = temp;
    }
    LineSegment visibleSegment = createdSegment.getStart().equals(top.point()) ? createdSegment : createdSegment.getSibling();
    var topPrevNext = prevNextMap.get(top.point());
    var bottomPrevNext = prevNextMap.get(bottom.point());
    prevNextMap.put(top.point(), new PrevNext(topPrevNext.prev(), visibleSegment));
    prevNextMap.put(bottom.point(), new PrevNext(visibleSegment, bottomPrevNext.next()));
  }

  private void addFromBottomToTop(BoundaryPoint top, BoundaryPoint bottom, LineSegment createdSegment) {
    if (top.point().getY() < bottom.point().getY()) {
      var temp = top;
      top = bottom;
      bottom = temp;
    }
    LineSegment visibleSegment = createdSegment.getStart().equals(bottom.point()) ? createdSegment : createdSegment.getSibling();
    var topPrevNext = prevNextMap.get(top.point());
    var bottomPrevNext = prevNextMap.get(bottom.point());
    prevNextMap.put(top.point(), new PrevNext(visibleSegment, topPrevNext.next()));
    prevNextMap.put(bottom.point(), new PrevNext(bottomPrevNext.prev(), visibleSegment));
  }

  private void addFromRightToLeft(BoundaryPoint right, BoundaryPoint left, LineSegment createdSegment) {
    if (right.isLeftBoundary()) {
      var temp = right;
      right = left;
      left = temp;
    }
    LineSegment visibleSegment = createdSegment.getStart().equals(right.point()) ? createdSegment : createdSegment.getSibling();
    var rightPrevNext = prevNextMap.get(right.point());
    var leftPrevNext = prevNextMap.get(left.point());
    prevNextMap.put(right.point(), new PrevNext(rightPrevNext.prev(), visibleSegment));
    prevNextMap.put(left.point(), new PrevNext(visibleSegment, leftPrevNext.next()));
  }
}
