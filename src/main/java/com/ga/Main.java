package com.ga;

import com.ga.io.Reader;

import java.nio.file.Path;
import java.util.Arrays;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Provide path to instances");
      System.exit(1);
    }
    System.out.println(Arrays.toString(args));
    var problemInstances = Reader.getProblemInstances(Path.of(args[0]));
    System.out.printf("Successfully read %d problem instances\n", problemInstances.size());
  }

}
