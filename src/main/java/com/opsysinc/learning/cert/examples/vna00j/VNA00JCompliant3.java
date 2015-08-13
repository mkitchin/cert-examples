package com.opsysinc.learning.cert.examples.vna00j;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * VNA00-J. Ensure visibility when accessing shared primitive variables.
 * <p/>
 * Compliant example #3 (synchronization).
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public class VNA00JCompliant3 extends VNA00JBase {

    /**
     * Current value.
     */
    private int currentValue;

    /**
     * Basic ctor.
     *
     * @param testLengthInMs Test length in MS.
     */
    public VNA00JCompliant3(final long testLengthInMs) {

        super(testLengthInMs);
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildReaderWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(true) {

            @Override
            protected void runImpl() {

                this.logSample(VNA00JCompliant3.this.getCurrentValue(), 0L);
            }
        };
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildWriterWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(false) {

            @Override
            protected void runImpl() {

                this.logSample(VNA00JCompliant3.this.setCurrentValue(), 0L);
            }
        };
    }

    /**
     * Gets current value.
     *
     * @return Current value.
     */
    private synchronized int getCurrentValue() {

        return this.currentValue;
    }

    /**
     * Sets & returns current value.
     *
     * @return Current value.
     */
    private synchronized int setCurrentValue() {

        this.currentValue++;
        return this.currentValue;
    }
}
