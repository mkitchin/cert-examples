package com.opsysinc.learning.cert.examples.vna00j;

import com.opsysinc.learning.cert.examples.util.ReaderWriterBase;

/**
 * VNA00-J. Ensure visibility when accessing shared primitive variables.
 * <p/>
 * Non-compliant example.
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public class VNA00JNonCompliant extends VNA00JBase {

    /**
     * Current value.
     */
    private int currentValue;

    /**
     * Basic ctor.
     *
     * @param testLengthInMs Test length in MS.
     */
    public VNA00JNonCompliant(final String id, final long testLengthInMs) {

        super(id, testLengthInMs);
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildReaderWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(true) {

            @Override
            protected void runImpl() {

                this.logSample(VNA00JNonCompliant.this.currentValue);
            }
        };
    }

    @Override
    protected ReaderWriterBase.ReaderWriterWorker<Integer> buildWriterWorker() {

        return new ReaderWriterBase.ReaderWriterWorker<Integer>(false) {

            @Override
            protected void runImpl() {

                this.logSample(++VNA00JNonCompliant.this.currentValue);
            }
        };
    }
}
