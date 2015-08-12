package com.opsysinc.learning.cert.examples;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;
import com.opsysinc.learning.cert.examples.vna00j.VNA00JBase;
import com.opsysinc.learning.cert.examples.vna00j.VNA00JCompliant1;
import com.opsysinc.learning.cert.examples.vna00j.VNA00JCompliant2;
import com.opsysinc.learning.cert.examples.vna00j.VNA00JNonCompliant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Main class.
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public final class Main {

    /**
     * Default parallel tests.
     */
    private static final int DEFAULT_PARALLEL_TESTS = 250;

    /**
     * Main driver method.
     *
     * @param args Args.
     */
    public static void main(final String[] args) {

        int parallelTests = -1;
        long testLengthInMs = -1;

        try {

            if (args.length > 0) {

                parallelTests = Integer.valueOf(args[0].trim());
            }

            if (args.length > 1) {

                testLengthInMs = Long.valueOf(args[1].trim());
            }

            if (parallelTests < 1) {

                parallelTests = Main.DEFAULT_PARALLEL_TESTS;
            }

            Main.testVNA00JNonCompliant(parallelTests, testLengthInMs);
            Main.testVNA00JCompliant1(parallelTests, testLengthInMs);
            Main.testVNA00JCompliant2(parallelTests, testLengthInMs);

        } catch (final Exception ex) {

            ex.printStackTrace();
        }
    }

    /**
     * Test non-compliant.
     *
     * @param parallelTests  Parallel test count.
     * @param testLengthInMs Test length in MS.
     * @throws InterruptedException
     */
    private static void testVNA00JNonCompliant(final int parallelTests,
                                               final long testLengthInMs)
            throws InterruptedException {

        final List<VNA00JBase> tests = new ArrayList<>();
        final List<Callable<Object>> tasks = new ArrayList<>();

        for (int ctr = 0; ctr < parallelTests; ctr++) {

            final VNA00JBase test = new VNA00JNonCompliant(testLengthInMs);

            tests.add(test);
            tasks.add(Executors.callable(test));
        }

        System.out.println("\nVNA00J: Non-Compliant.");
        System.out.println("\nExecuting " + parallelTests + " tests...");

        final ExecutorService executorService = Executors.newFixedThreadPool(parallelTests, new ThreadFactory() {

            @Override
            public Thread newThread(final Runnable runnable) {

                final Thread result = new Thread(runnable);
                result.setDaemon(true);

                return result;
            }
        });

        executorService.invokeAll(tasks);
        executorService.shutdown();

        System.out.println("Checking " + parallelTests + " logs...");

        int ctr = 0;
        int outOfOrderCtr = 0;

        for (final ReaderWriterBase item : tests) {

            ctr++;

            if (item.checkOrder()) {

                System.out.println("\nTest #" + ctr + ": OUT OF ORDER");
                item.dumpLogs();

                outOfOrderCtr++;
            }
        }

        System.out.println(String.format("\n%d of %d logs out of order.",
                outOfOrderCtr, parallelTests));
        System.out.println("\n...Done.");
    }

    /**
     * Test compliant #1.
     *
     * @param parallelTests  Parallel test count.
     * @param testLengthInMs Test length in MS.
     * @throws InterruptedException
     */
    private static void testVNA00JCompliant1(final int parallelTests,
                                             final long testLengthInMs)
            throws InterruptedException {

        final List<VNA00JBase> tests = new ArrayList<>();
        final List<Callable<Object>> tasks = new ArrayList<>();

        for (int ctr = 0; ctr < parallelTests; ctr++) {

            final VNA00JBase test = new VNA00JCompliant1(testLengthInMs);

            tests.add(test);
            tasks.add(Executors.callable(test));
        }

        System.out.println("\nVNA00J: Compliant #1 (volatile qualifier).");
        System.out.println("\nExecuting " + parallelTests + " tests...");

        final ExecutorService executorService = Executors.newFixedThreadPool(parallelTests, new ThreadFactory() {

            @Override
            public Thread newThread(final Runnable runnable) {

                final Thread result = new Thread(runnable);
                result.setDaemon(true);

                return result;
            }
        });

        executorService.invokeAll(tasks);
        executorService.shutdown();

        System.out.println("Checking " + parallelTests + " logs...");

        int ctr = 0;
        int outOfOrderCtr = 0;

        for (final ReaderWriterBase item : tests) {

            ctr++;

            if (item.checkOrder()) {

                System.out.println("\nTest #" + ctr + ": OUT OF ORDER");
                item.dumpLogs();

                outOfOrderCtr++;
            }
        }

        System.out.println(String.format("\n%d of %d logs out of order.",
                outOfOrderCtr, parallelTests));
        System.out.println("\n...Done.");
    }

    /**
     * Test compliant #2.
     *
     * @param parallelTests  Parallel test count.
     * @param testLengthInMs Test length in MS.
     * @throws InterruptedException
     */
    private static void testVNA00JCompliant2(final int parallelTests,
                                             final long testLengthInMs)
            throws InterruptedException {

        final List<VNA00JBase> tests = new ArrayList<>();
        final List<Callable<Object>> tasks = new ArrayList<>();

        for (int ctr = 0; ctr < parallelTests; ctr++) {

            final VNA00JBase test = new VNA00JCompliant2(testLengthInMs);

            tests.add(test);
            tasks.add(Executors.callable(test));
        }

        System.out.println("\nVNA00J: Compliant #2 (AtomicInteger).");
        System.out.println("\nExecuting " + parallelTests + " tests...");

        final ExecutorService executorService = Executors.newFixedThreadPool(parallelTests, new ThreadFactory() {

            @Override
            public Thread newThread(final Runnable runnable) {

                final Thread result = new Thread(runnable);
                result.setDaemon(true);

                return result;
            }
        });

        executorService.invokeAll(tasks);
        executorService.shutdown();

        System.out.println("Checking " + parallelTests + " logs...");

        int ctr = 0;
        int outOfOrderCtr = 0;

        for (final ReaderWriterBase item : tests) {

            ctr++;

            if (item.checkOrder()) {

                System.out.println("\nTest #" + ctr + ": OUT OF ORDER");
                item.dumpLogs();

                outOfOrderCtr++;
            }
        }

        System.out.println(String.format("\n%d of %d logs out of order.",
                outOfOrderCtr, parallelTests));
        System.out.println("\n...Done.");
    }
}
