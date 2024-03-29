package com.ga.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Point {
  private long x, y;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JsonIgnore
  private List<LineSegment> segments = new ArrayList<>();
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @JsonIgnore
  private boolean isHole = false;

  public Point(long x, long y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point other) {
    this.x = other.x;
    this.y = other.y;
    this.isHole = other.isHole;
  }
}
