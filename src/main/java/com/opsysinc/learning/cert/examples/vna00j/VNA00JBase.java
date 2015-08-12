package com.opsysinc.learning.cert.examples.vna00j;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;

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
    private static final long DEFAULT_TEST_LENGTH_IN_MS = 30000L;

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


            this.startUp(this.buildReaderWorker(),
                    this.buildWriterWorker());
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
    protected abstract Runnable buildReaderWorker();

    /**
     * Builds writer worker.
     *
     * @return Writer worker.
     */
    protected abstract Runnable buildWriterWorker();
}
