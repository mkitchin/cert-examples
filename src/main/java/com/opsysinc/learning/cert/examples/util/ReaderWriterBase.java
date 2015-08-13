package com.opsysinc.learning.cert.examples.util;


import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Reader/writer base class.
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public abstract class ReaderWriterBase<T extends Comparable> {

    /**
     * Reader threads.
     */
    private List<Thread> workerThreads;

    /**
     * Reader workers.
     */
    private List<ReaderWriterBase.ReaderWriterWorker<T>> readerWorkers;

    /**
     * Writer workers.
     */
    private List<ReaderWriterBase.ReaderWriterWorker<T>> writerWorkers;

    /**
     * My id.
     */
    private final String id;

    /**
     * Basic ctor.
     *
     * @param id My id.
     */
    public ReaderWriterBase(final String id) {

        this.id = id;
    }

    /**
     * Start up.
     *
     * @param readerWorkers Readers.
     * @param writerWorkers Writers.
     */
    public void startUp(final List<ReaderWriterBase.ReaderWriterWorker<T>> readerWorkers,
                        final List<ReaderWriterBase.ReaderWriterWorker<T>> writerWorkers) {

        if (this.workerThreads == null) {

            this.readerWorkers = readerWorkers;
            this.writerWorkers = writerWorkers;

            final int totalWorkers = (this.readerWorkers.size() + this.writerWorkers.size());

            final CountDownLatch startUpLatch = new CountDownLatch(totalWorkers + 1);
            final CyclicBarrier workerBarrier = new CyclicBarrier(totalWorkers, new Runnable() {

                @Override
                public void run() {

                    try {

                        ReaderWriterBase.this.checkWorkers();

                    } catch (final InterruptedException ex) {

                        // ignore;
                    }
                }
            });

            this.workerThreads = new ArrayList<>();

            for (final ReaderWriterBase.ReaderWriterWorker<T> item : readerWorkers) {

                item.setStartUpLatch(startUpLatch);
                item.setWorkBarrier(workerBarrier);

                final Thread readerThread = new Thread(item);

                readerThread.setDaemon(true);
                readerThread.start();

                this.workerThreads.add(readerThread);
            }

            for (final ReaderWriterBase.ReaderWriterWorker<T> item : writerWorkers) {

                item.setStartUpLatch(startUpLatch);
                item.setWorkBarrier(workerBarrier);

                final Thread writerThread = new Thread(item);

                writerThread.setDaemon(true);
                writerThread.start();

                this.workerThreads.add(writerThread);
            }

            startUpLatch.countDown();
        }
    }

    /**
     * Checks workers.
     */
    private void checkWorkers() throws InterruptedException {

        final long workTime = System.nanoTime();

        final T expectedValue = this.nextExpectedValue();
        T highestWriterValue = null;

        for (final List<ReaderWriterBase.ReaderWriterWorker<T>> listItem :
                Arrays.asList(this.writerWorkers, this.readerWorkers)) {

            for (final ReaderWriterBase.ReaderWriterWorker<T> workerItem : listItem) {

                workerItem.setWorkTime(workTime);

                if (!workerItem.isReader()) {

                    final T prevValue = workerItem.getPrevData();

                    if (prevValue != null) {

                        if ((highestWriterValue == null) ||
                                (highestWriterValue.compareTo(prevValue) < 0)) {

                            highestWriterValue = prevValue;
                        }
                    }
                }
            }
        }

        boolean isOutOfOrder = false;

        if ((expectedValue != null) &&
                (highestWriterValue != null)) {

            if (!expectedValue.equals(highestWriterValue)) {

                isOutOfOrder = true;
            }
        }

        if (isOutOfOrder) {

            synchronized (System.out) {

                System.out.println(String.format("\n%s - WRITERS OUT OF ORDER (stopping)", this.id));
                System.out.println(String.format("Expected value=%s\n", String.valueOf(expectedValue)));

                for (final List<ReaderWriterBase.ReaderWriterWorker<T>> listItem :
                        Arrays.asList(this.writerWorkers, this.readerWorkers)) {

                    for (final ReaderWriterBase.ReaderWriterWorker<T> workerItem : listItem) {

                        final T prevValue = workerItem.getPrevData();

                        if (prevValue != null) {

                            System.out.println(String.format("%s / %s / %s%s",
                                    (workerItem.isReader() ? "READER" : "WRITER"),
                                    String.valueOf(workTime),
                                    String.valueOf(prevValue),
                                    ((!workerItem.isReader() && !prevValue.equals(expectedValue)) ? " / ***" : "")));
                        }
                    }
                }

                this.cleanUp();
            }
        }
    }

    /**
     * Clean up.
     */
    public void cleanUp() throws InterruptedException {

        if (this.workerThreads != null) {

            for (final Thread item : this.workerThreads) {

                item.interrupt();
            }

            for (final Thread item : this.workerThreads) {

                item.join();
            }

            this.workerThreads = null;
        }
    }

    /**
     * Gets next expected value.
     *
     * @return Next expected value.
     */
    protected abstract T nextExpectedValue();

    /**
     * Base worker class.
     */
    protected abstract static class ReaderWriterWorker<T> implements Runnable {

        /**
         * Last reader data.
         */
        private T prevData;

        /**
         * True if reader, false otherwise.
         */
        private final boolean isReader;

        /**
         * Start up latch.
         */
        private CountDownLatch startUpLatch;

        /**
         * Work barrier
         */
        private CyclicBarrier workBarrier;

        /**
         * Work time.
         */
        private long workTime;

        /**
         * Basic ctor.
         *
         * @param isReader True if reader, false otherwise.
         */
        public ReaderWriterWorker(final boolean isReader) {

            this.isReader = isReader;
        }

        @Override
        public void run() {

            try {

                this.startUpLatch.countDown();
                this.startUpLatch.await();

                while (!Thread.interrupted()) {

                    this.workBarrier.await();

                    this.runImpl();
                    Thread.sleep(1L);
                }

            } catch (final InterruptedException |
                    BrokenBarrierException ex) {

                // ignore;

            } catch (final Exception ex) {

                ex.printStackTrace();
            }
        }

        /**
         * Run implementation.
         */
        protected abstract void runImpl();

        /**
         * Gets is reader flag.
         *
         * @return True if reader, false otherwise.
         */
        public boolean isReader() {

            return this.isReader;
        }

        /**
         * Log sample.
         *
         * @param data Sample data.
         */
        public void logSample(final T data) {

            this.prevData = data;
        }

        /**
         * Sets startup latch.
         *
         * @param startUpLatch Startup latch.
         */
        public void setStartUpLatch(final CountDownLatch startUpLatch) {

            this.startUpLatch = startUpLatch;
        }

        /**
         * Gets prev data.
         *
         * @return Prev data.
         */
        public T getPrevData() {

            return this.prevData;
        }

        /**
         * Sets work barrier.
         *
         * @param workBarrier Work barrier.
         */
        public void setWorkBarrier(final CyclicBarrier workBarrier) {

            this.workBarrier = workBarrier;
        }

        /**
         * Sets work time.
         *
         * @param workTime Work time.
         */
        public void setWorkTime(final long workTime) {

            this.workTime = workTime;
        }
    }
}
