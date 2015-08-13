package com.opsysinc.learning.cert.examples.vna00j;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
     * Default writer workers.
     */
    private static final int DEFAULT_WRITER_WORKERS = 10;

    /**
     * Default reader workers.
     */
    private static final int DEFAULT_READER_WORKERS = 10;

    /**
     * Test length in MS.
     */
    private final long testLengthInMs;

    /**
     * Expected value.
     */
    private final AtomicInteger nextExpectedValue;

    /**
     * Basic ctor.
     *
     * @param testLengthInMs Test length in MS.
     */
    public VNA00JBase(final String id, final long testLengthInMs) {

        super(id);
        this.testLengthInMs = ((testLengthInMs < 1L) ?
                VNA00JBase.DEFAULT_TEST_LENGTH_IN_MS : testLengthInMs);
        this.nextExpectedValue = new AtomicInteger((VNA00JBase.DEFAULT_WRITER_WORKERS * -1));
    }

    @Override
    public void run() {

        try {

            final List<ReaderWriterBase.ReaderWriterWorker<Integer>> readerWorkers = new ArrayList<>();

            for (int ctr = 0; ctr < VNA00JBase.DEFAULT_READER_WORKERS; ctr++) {

                readerWorkers.add(this.buildReaderWorker());
            }

            final List<ReaderWriterBase.ReaderWriterWorker<Integer>> writerWorkers = new ArrayList<>();

            for (int ctr = 0; ctr < VNA00JBase.DEFAULT_WRITER_WORKERS; ctr++) {

                writerWorkers.add(this.buildWriterWorker());
            }

            this.startUp(readerWorkers, writerWorkers);
            Thread.sleep(this.testLengthInMs);

            this.cleanUp();

        } catch (final Exception ex) {

            ex.printStackTrace();
        }
    }

    @Override
    protected Integer nextExpectedValue() {

        return this.nextExpectedValue.addAndGet(VNA00JBase.DEFAULT_WRITER_WORKERS);
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
