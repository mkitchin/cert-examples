package com.opsysinc.learning.cert.examples.vna00j;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * VNA00-J. Ensure visibility when accessing shared primitive variables.
 * <p/>
 * Base capabilities.
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public abstract class VNA00JBase extends ReaderWriterBase<Integer> implements Runnable {

    /**
     * Default test length in MS.
     */
    private static final long DEFAULT_TEST_LENGTH_IN_MS = 60000L;

    /**
     * Test length in MS.
     */
    private final long testLengthInMs;

    /**
     * Basic ctor.
     *
     * @param testLengthInMs Test length in MS.
     */
    public VNA00JBase(final long testLengthInMs) {

        this.testLengthInMs = ((testLengthInMs < 1L) ?
                VNA00JBase.DEFAULT_TEST_LENGTH_IN_MS : testLengthInMs);
    }

    @Override
    public void run() {

        try {

            final List<ReaderWriterBase.ReaderWriterWorker<Integer>> readerWorkers = new ArrayList<>();

            for (int ctr = 0; ctr < 10; ctr++) {

                readerWorkers.add(this.buildReaderWorker());
            }

            this.startUp(readerWorkers, Collections.singletonList(this.buildWriterWorker()));
            Thread.sleep(this.testLengthInMs);

            this.cleanUp();

        } catch (final Exception ex) {

            ex.printStackTrace();
        }
    }

    /**
     * Builds reader worker.
     *
     * @return Reader worker.
     */
    protected abstract ReaderWriterBase.ReaderWriterWorker<Integer> buildReaderWorker();

    /**
     * Builds writer worker.
     *
     * @return Writer worker.
     */
    protected abstract ReaderWriterBase.ReaderWriterWorker<Integer> buildWriterWorker();
}
