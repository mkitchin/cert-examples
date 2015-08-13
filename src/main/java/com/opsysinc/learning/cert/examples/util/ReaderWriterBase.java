package com.opsysinc.learning.cert.examples.util;


import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;

/**
 * Reader/writer base class.
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public class ReaderWriterBase<T extends Comparable> {

    /**
     * Timing map.
     */
    private Map<Long, List<ReaderWriterBase.SampleEntry<T>>> sortedSamples;

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
     * Start up.
     *
     * @param readerWorkers Readers.
     * @param writerWorkers Writers.
     */
    public void startUp(final List<ReaderWriterBase.ReaderWriterWorker<T>> readerWorkers,
                        final List<ReaderWriterBase.ReaderWriterWorker<T>> writerWorkers) {

        this.readerWorkers = readerWorkers;
        this.writerWorkers = writerWorkers;

        final int totalWorkers = (this.readerWorkers.size() + this.writerWorkers.size());

        final CountDownLatch startUpLatch = new CountDownLatch(totalWorkers + 1);
        final CyclicBarrier workerBarrier = new CyclicBarrier(totalWorkers, new Runnable() {

            @Override
            public void run() {

                ReaderWriterBase.this.setWorkTime(System.nanoTime());
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

    /**
     * Set work time.
     *
     * @param workTime Work time.
     */
    private void setWorkTime(final long workTime) {

        for (final List<ReaderWriterBase.ReaderWriterWorker<T>> listItem :
                Arrays.asList(this.writerWorkers, this.readerWorkers)) {

            for (final ReaderWriterBase.ReaderWriterWorker<T> workerItem : listItem) {

                workerItem.setWorkTime(workTime);
            }
        }
    }

    /**
     * Clean up.
     */
    public void cleanUp() throws InterruptedException {

        for (final Thread item : this.workerThreads) {

            item.interrupt();
        }

        for (final Thread item : this.workerThreads) {

            item.join();
        }

        this.workerThreads = null;
    }

    /**
     * Build timing map.
     *
     * @param isToForce True to force build, false otherwise.
     */
    private void checkSortedSamples(final boolean isToForce) {

        if (isToForce ||
                (this.sortedSamples == null)) {

            final Map<Long, List<ReaderWriterBase.SampleEntry<T>>> tempSortedSamples = new TreeMap<>();
            final int totalWorkers = (this.readerWorkers.size() + this.writerWorkers.size());

            for (final List<ReaderWriterBase.ReaderWriterWorker<T>> listItem :
                    Arrays.asList(this.writerWorkers, this.readerWorkers)) {

                for (final ReaderWriterBase.ReaderWriterWorker<T> workerItem : listItem) {

                    for (final ReaderWriterBase.SampleEntry<T> sampleItem : workerItem.getSampleLog()) {

                        List<ReaderWriterBase.SampleEntry<T>> sampleList = tempSortedSamples.get(sampleItem.getTime());

                        if (sampleList == null) {

                            sampleList = new ArrayList<>(totalWorkers);
                            tempSortedSamples.put(sampleItem.getTime(), sampleList);
                        }

                        sampleList.add(sampleItem);
                    }
                }
            }

            this.sortedSamples = tempSortedSamples;
        }
    }

    /**
     * Clear sorted samples.
     */
    public void clearSamples() {

        this.sortedSamples = null;

        for (final List<ReaderWriterBase.ReaderWriterWorker<T>> listItem :
                Arrays.asList(this.writerWorkers, this.readerWorkers)) {

            for (final ReaderWriterBase.ReaderWriterWorker<T> workerItem : listItem) {

                workerItem.getSampleLog().clear();
            }
        }
    }

    /**
     * Check order.
     *
     * @return True if reader/writer logs out of order, false otherwise.
     */
    public boolean checkOrder() {

        boolean result = false;
        this.checkSortedSamples(false);

        for (final Map.Entry<Long, List<ReaderWriterBase.SampleEntry<T>>> entryItem : this.sortedSamples.entrySet()) {

            T lowestReaderValue = null;
            T highestWriterValue = null;

            for (final ReaderWriterBase.SampleEntry<T> sampleItem : entryItem.getValue()) {

                final T sampleValue = sampleItem.getData();

                if (sampleItem.isReader()) {

                    if ((lowestReaderValue == null) ||
                            (lowestReaderValue.compareTo(sampleValue) > 0)) {

                        lowestReaderValue = sampleValue;
                    }

                } else {

                    if ((highestWriterValue == null) ||
                            (highestWriterValue.compareTo(sampleValue) < 0)) {

                        highestWriterValue = sampleValue;
                    }
                }
            }

            if ((lowestReaderValue != null) && (highestWriterValue != null) &&
                    (highestWriterValue.compareTo(lowestReaderValue) > 0)) {

                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Dump logs.
     */
    public void dumpLogs() {

        int readerSamples = 0;
        int writerSamples = 0;

        for (final List<ReaderWriterBase.ReaderWriterWorker<T>> listItem :
                Arrays.asList(this.writerWorkers, this.readerWorkers)) {

            for (final ReaderWriterBase.ReaderWriterWorker<T> workerItem : listItem) {

                if (workerItem.isReader()) {

                    readerSamples += workerItem.getSampleLog().size();

                } else {

                    writerSamples += workerItem.getSampleLog().size();
                }
            }
        }

        System.out.println(String.format("\nTime samples - total=%d",
                this.sortedSamples.size()));
        System.out.println(String.format("Log entries - reader=%d, writer=%d, total=%d",
                readerSamples, writerSamples, (readerSamples + writerSamples)));
        this.checkSortedSamples(false);

        int sampleCtr = 0;
        int outOfOrderCtr = 0;

        for (final Map.Entry<Long, List<ReaderWriterBase.SampleEntry<T>>> entryItem : this.sortedSamples.entrySet()) {

            sampleCtr++;

            T lowestReaderValue = null;
            T highestWriterValue = null;

            for (final ReaderWriterBase.SampleEntry<T> sampleItem : entryItem.getValue()) {

                final T sampleValue = sampleItem.getData();

                if (sampleItem.isReader()) {

                    if ((lowestReaderValue == null) ||
                            (lowestReaderValue.compareTo(sampleValue) > 0)) {

                        lowestReaderValue = sampleValue;
                    }

                } else {

                    if ((highestWriterValue == null) ||
                            (highestWriterValue.compareTo(sampleValue) < 0)) {

                        highestWriterValue = sampleValue;
                    }
                }
            }

            if ((lowestReaderValue != null) && (highestWriterValue != null) &&
                    (highestWriterValue.compareTo(lowestReaderValue) > 0)) {

                outOfOrderCtr++;
                System.out.println(String.format("\nEntry #%d:\n", sampleCtr));

                for (final ReaderWriterBase.SampleEntry<T> sampleItem : entryItem.getValue()) {

                    System.out.println(String.format("%s / %s / %s%s",
                            (sampleItem.isReader() ? "READER" : "WRITER"),
                            String.valueOf(sampleItem.getTime()),
                            String.valueOf(sampleItem.getData()),
                            ((sampleItem.isReader() && sampleItem.getData().equals(lowestReaderValue)) ? " / ***" : "")));
                }

                if (outOfOrderCtr > 4) {

                    System.out.println(String.format("\n((Skipping %d time samples))",
                            (this.sortedSamples.size() - sampleCtr)));
                    break;
                }
            }
        }
    }

    /**
     * Base worker class.
     */
    protected abstract static class ReaderWriterWorker<T> implements Runnable {

        /**
         * Reader log.
         */
        private final List<ReaderWriterBase.SampleEntry<T>> sampleLog;

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
            this.sampleLog = new LinkedList<>();
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
         * @param time Sample time.
         */
        public void logSample(final T data,
                              final long time) {

            this.prevData = data;
            this.sampleLog.add(new ReaderWriterBase.SampleEntry<>(this.isReader, data,
                    ((time < 1L) ? this.workTime : time)));
        }

        /**
         * Gets sample log.
         *
         * @return Sample log.
         */
        public List<ReaderWriterBase.SampleEntry<T>> getSampleLog() {

            return this.sampleLog;
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

    /**
     * Timing entry.
     */
    protected static class SampleEntry<T> {

        /**
         * Data.
         */
        private final T data;

        /**
         * Ctr.
         */
        private final long time;

        /**
         * True if reader, false otherwise.
         */
        private final boolean isReader;

        /**
         * Basic ctor.
         *
         * @param isReader True if reader, false otherwise.
         * @param data     Sample data.
         * @param time     Sample time.
         */
        public SampleEntry(final boolean isReader,
                           final T data,
                           final long time) {

            this.isReader = isReader;
            this.data = data;
            this.time = time;
        }

        /**
         * Gets data.
         *
         * @return Data.
         */
        public T getData() {

            return this.data;
        }

        /**
         * Gets time.
         *
         * @return Time.
         */
        public long getTime() {

            return this.time;
        }

        /**
         * Gets is reader flag.
         *
         * @return True if reader, false otherwise.
         */
        public boolean isReader() {

            return this.isReader;
        }
    }

}
