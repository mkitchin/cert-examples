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
    public VNA00JNonCompliant(final long testLengthInMs) {

        super(testLengthInMs);
    }

    @Override
    protected Runnable buildReaderWorker() {

        return new ReaderWriterBase.WorkerBase() {

            @Override
            protected void runImpl() {

                VNA00JNonCompliant.this.logReader(VNA00JNonCompliant.this.currentValue,
                        System.currentTimeMillis());
            }

            @Override
            protected long sleepTimeInMs() {

                return 1L;
            }
        };
    }

    @Override
    protected Runnable buildWriterWorker() {

        return new ReaderWriterBase.WorkerBase() {

            /**
             * Counter.
             */
            private long counter;

            @Override
            protected void runImpl() {

                VNA00JNonCompliant.this.logWriter(++VNA00JNonCompliant.this.currentValue,
                        System.currentTimeMillis());
            }

            @Override
            protected long sleepTimeInMs() {

                return 1L;
            }
        };
    }
}
