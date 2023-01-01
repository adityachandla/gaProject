package com.ga.triangulation;

import com.ga.data.BoundaryPoint;
import com.ga.data.BoundarySegment;
import com.ga.data.LineSegment;
import com.ga.monotone.SegmentAdder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TriangulationEdgeGenerator {

  private final List<BoundaryPoint> boundaryPoints;
  private Stack<BoundaryPoint> stack;
  private List<LineSegment> addedSegments;

  public TriangulationEdgeGenerator(List<BoundaryPoint> boundaryPoints) {
    this.boundaryPoints = boundaryPoints;
  }

  private void initializeStack() {
    stack = new Stack<>();
    stack.add(boundaryPoints.get(0));
    stack.add(boundaryPoints.get(1));
  }

  public List<LineSegment> createTriangulation() {
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
    BoundaryPoint lastProcessed = null;
    while (stack.size() > 1) {
      var toProcess = stack.pop();
      lastProcessed = toProcess;
      var addedSegment = addSegment(p, toProcess);
      addedSegments.add(addedSegment);
    }
    stack.pop();
    stack.push(lastProcessed);
    stack.push(p);
  }

  private void processSameBoundaries(BoundaryPoint p) {
    var popped = stack.pop();
    var prev = popped;
    var boundary = new BoundarySegment(p, popped);
    while (!stack.empty() && boundary.sameBoundary(stack.peek())) {
      var toProcess = stack.pop();
      prev = toProcess;
      var addedSegment = addSegment(p, toProcess);
      addedSegments.add(addedSegment);
    }
    stack.push(prev);
    stack.push(p);
  }

  private void processLastPoint() {
    var last = boundaryPoints.get(boundaryPoints.size() - 1);
    if (!stack.empty()) stack.pop();
    while (stack.size() > 1) {
      var toProcess = stack.pop();
      var addedSegment = addSegment(last, toProcess);
      addedSegments.add(addedSegment);
    }
  }


  private LineSegment addSegment(BoundaryPoint one, BoundaryPoint two) {
    if (one.isLeftBoundary()) {
      var temp = one;
      one = two;
      two = temp;
    }
    return SegmentAdder.createSegment(one.point(), two.point());
  }
}
