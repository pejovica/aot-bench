package com.oracle.svm.bench.shootouts;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Consumer;

class BenchRunner {

    static void run(Consumer<String[]> bench, String benchName, String[] args) {
        Integer nRuns = Integer.valueOf(args[1]);
        String[] benchArgs = Arrays.copyOfRange(args, 2, args.length);

        PrintStream systemOut = System.out;

        // let benches write to memory
        ByteArrayOutputStream benchOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(benchOut));

        systemOut.println("---- BEGIN WARM UP ----");
        systemOut.println("Performing " + args[0] + " rounds.");
        systemOut.println();
        for (int i = 0; i < Integer.valueOf(args[0]); i++) {
            bench.accept(benchArgs);
            benchOut.reset();
        }
        systemOut.println("---- END WARM UP ----");

// System.gc();

        systemOut.println("---- BEGIN MEASURING PERFORMANCE ----");
        systemOut.println("Performing " + args[1] + " runs.");
        systemOut.println();
        long start = System.nanoTime();
        for (int i = 0; i < nRuns; i++) {
            benchOut.reset();   // reset before a run, so that we get to keep the result of the last run
            bench.accept(benchArgs);
        }
        long stop = System.nanoTime();
        systemOut.println("---- END MEASURING PERFORMANCE ----");

        systemOut.println("---- BEGIN OUTPUT ----");
        systemOut.print(benchOut.toString());
        systemOut.println("---- END OUTPUT ----");

        systemOut.println();
        systemOut.println("*** " + benchName.toUpperCase() + " RESULTS: Average time based on " + args[1] + " runs is " + String.valueOf((stop - start) / nRuns * 10e-3) + " us ***");
        systemOut.println();
    }
}
