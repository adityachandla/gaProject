package com.ga.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
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
public class Point {
  private int x, y;
  @ToString.Exclude
  private List<LineSegment> segments = new ArrayList<>();
  @ToString.Exclude
  private boolean isHole = false;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
