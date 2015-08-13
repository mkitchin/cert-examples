package com.opsysinc.learning.cert.examples.vna00j;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;

/**
 * VNA00-J. Ensure visibility when accessing shared primitive variables.
 * <p/>
 * Compliant example #1 (volatile qualifier).
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public class VNA00JCompliant1 extends VNA00JBase {

    /**
     * Current value.
     */
    private volatile int currentValue;

    /**
     * Basic ctor.
     *
     * @param testLengthInMs Test length in MS.
     */
    public VNA00JCompliant1(final long testLengthInMs) {

        super(testLengthInMs);
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildReaderWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(true) {

            @Override
            protected void runImpl() {

                this.logSample(VNA00JCompliant1.this.currentValue, System.currentTimeMillis());
            }
        };
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildWriterWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(false) {

            @Override
            protected void runImpl() {

                this.logSample(++VNA00JCompliant1.this.currentValue, System.currentTimeMillis());
            }
        };
    }
}
