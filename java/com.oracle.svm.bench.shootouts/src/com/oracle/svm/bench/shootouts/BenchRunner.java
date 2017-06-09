package com.oracle.svm.bench.shootouts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Consumer;

class BenchRunner {

    static void run(Consumer<String[]> bench, String benchName, String[] args) {
        String[] benchArgs = Arrays.copyOfRange(args, 2, args.length);
        bench.accept(benchArgs);

        System.out.println("---- BEGIN WARM UP ----");
        System.out.println("Performing " + args[0] + " rounds.");
        System.out.println();

        // ignore output
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        })
                        );
        for (int i = 0; i < Integer.valueOf(args[0]); i++) {
            bench.accept(benchArgs);
        }
        // restore output
        System.setOut(out);
        System.out.println("---- END WARM UP ----");

// System.gc();

        System.out.println("---- BEGIN MEASURING PERFORMANCE ----");
        System.out.println("Performing " + args[1] + " runs.");
        System.out.println();

        Integer nRuns = Integer.valueOf(args[1]);
        long start = System.nanoTime();
        for (int i = 0; i < nRuns; i++) {
            bench.accept(benchArgs);
        }
        long stop = System.nanoTime();
        System.out.println("---- END MEASURING PERFORMANCE ----");

        System.out.println();
        System.out.println("*** " + benchName.toUpperCase() + " RESULTS: Average time based on " + args[1] + " runs is " + String.valueOf((stop - start) / nRuns * 10e-3) + " us ***");
        System.out.println();
    }
}
