package com.ga.data;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FaceReferenceGenerator {
  private static int referenceId = 0;

  public static int getAndIncrementReferenceId() {
    referenceId++;
    return referenceId;
  }

  public static void clear() {
    referenceId = 0;
  }

  public static int getCurrentReferenceId() {
    return referenceId;
  }
}
