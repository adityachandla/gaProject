package com.ga.convex;

public class FaceReferenceGenerator {
  private static int referenceId = 0;

  public static int getAndIncrementReferenceId() {
    referenceId++;
    return referenceId;
  }

  public static int getCurrentReferenceId() {
    return referenceId;
  }
}
