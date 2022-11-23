package com.ga.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Point {
  private int x, y;
  private List<LineSegment> segments; //Line segments this point is a part of

  public Point() {
    this.segments = new ArrayList<>();
  }
}
