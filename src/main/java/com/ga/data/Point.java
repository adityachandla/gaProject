package com.ga.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class Point {
  private long x, y;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<LineSegment> segments = new ArrayList<>();
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private boolean isHole = false;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
