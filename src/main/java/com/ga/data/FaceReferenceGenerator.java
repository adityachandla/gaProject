package com.ga.data;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class FaceReferenceGenerator {
  private static int referenceId = 1;

  public static int getAndIncrementReferenceId() {
    referenceId++;
    return referenceId;
  }

  public static void clear() {
    referenceId = 1;
  }

  public static void assignFaceIds(List<LineSegment> segments) {
    for (var segment: segments) {
      assignFaceId(segment);
    }
  }

  public static void assignFaceId(LineSegment segment) {
    var curr = segment;
    var referenceId = getAndIncrementReferenceId();
    do {
      curr.setFaceReferenceId(referenceId);
      curr = curr.getNext();
    } while(curr != segment);
  }
}
