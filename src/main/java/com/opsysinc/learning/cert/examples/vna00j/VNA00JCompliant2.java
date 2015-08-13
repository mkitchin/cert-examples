package com.opsysinc.learning.cert.examples.vna00j;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * VNA00-J. Ensure visibility when accessing shared primitive variables.
 * <p/>
 * Compliant example #1 (AtomicInteger).
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public class VNA00JCompliant2 extends VNA00JBase {

    /**
     * Current value.
     */
    private final AtomicInteger currentValue;

    /**
     * Basic ctor.
     *
     * @param testLengthInMs Test length in MS.
     */
    public VNA00JCompliant2(final long testLengthInMs) {

        super(testLengthInMs);
        this.currentValue = new AtomicInteger(0);
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildReaderWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(true) {

            @Override
            protected void runImpl() {

                this.logSample(VNA00JCompliant2.this.currentValue.get(), System.currentTimeMillis());
            }
        };
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildWriterWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(false) {

            @Override
            protected void runImpl() {

                this.logSample(VNA00JCompliant2.this.currentValue.incrementAndGet(), System.currentTimeMillis());
            }
        };
    }
}
