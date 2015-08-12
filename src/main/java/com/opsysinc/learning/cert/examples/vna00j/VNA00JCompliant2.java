package com.opsysinc.learning.cert.examples.vna00j;

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
    private AtomicInteger currentValue;

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
    protected Runnable buildReaderWorker() {

        return new WorkerBase() {

            @Override
            protected void runImpl() {

                VNA00JCompliant2.this.logReader(VNA00JCompliant2.this.currentValue.get());
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

                VNA00JCompliant2.this.logWriter(VNA00JCompliant2.this.currentValue.incrementAndGet());
            }

            @Override
            protected long sleepTimeInMs() {

                return 1L;
            }
        };
    }
}
