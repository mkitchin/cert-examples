package com.opsysinc.learning.cert.examples.vna00j;

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
    protected Runnable buildReaderWorker() {

        return new WorkerBase() {

            @Override
            protected void runImpl() {

                VNA00JCompliant3.this.logReader(VNA00JCompliant3.this.getCurrentValue());
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

                VNA00JCompliant3.this.logWriter(VNA00JCompliant3.this.setCurrentValue());
            }

            @Override
            protected long sleepTimeInMs() {

                return 1L;
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
