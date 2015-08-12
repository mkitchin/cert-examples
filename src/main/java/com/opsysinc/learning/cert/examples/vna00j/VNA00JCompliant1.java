package com.opsysinc.learning.cert.examples.vna00j;

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
    protected Runnable buildReaderWorker() {

        return new WorkerBase() {

            @Override
            protected void runImpl() {

                VNA00JCompliant1.this.logReader(VNA00JCompliant1.this.currentValue);
            }

            @Override
            protected long sleepTimeInMs() {

                return 1L;
            }
        };
    }

    @Override
    protected Runnable buildWriterWorker() {

        return new WorkerBase() {

            /**
             * Counter.
             */
            private long counter;

            @Override
            protected void runImpl() {

                VNA00JCompliant1.this.logWriter(++VNA00JCompliant1.this.currentValue);
            }

            @Override
            protected long sleepTimeInMs() {

                return 1L;
            }
        };
    }
}
