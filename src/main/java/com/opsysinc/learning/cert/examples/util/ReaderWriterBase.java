package com.opsysinc.learning.cert.examples.util;


import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reader/writer base class.
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public class ReaderWriterBase<T extends Comparable> {

    /**
     * Start up latch.
     */
    private CountDownLatch startUpLatch;

    /**
     * Timing map.
     */
    private Map<Long, List<ReaderWriterBase.SampleEntry<T>>> timingMap;

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
     * Log lock.
     */
    private ReentrantLock logLock;

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

        this.startUpLatch = new CountDownLatch(totalWorkers + 1);
        this.logLock = new ReentrantLock(true);

        this.workerThreads = new ArrayList<>();

        for (final ReaderWriterBase.ReaderWriterWorker<T> item : readerWorkers) {

            item.setStartUpLatch(this.startUpLatch);
            item.setLogLock(this.logLock);

            final Thread readerThread = new Thread(item);

            readerThread.setDaemon(true);
            readerThread.start();

            this.workerThreads.add(readerThread);
        }

        for (final ReaderWriterBase.ReaderWriterWorker<T> item : writerWorkers) {

            item.setStartUpLatch(this.startUpLatch);
            item.setLogLock(this.logLock);

            final Thread writerThread = new Thread(item);

            writerThread.setDaemon(true);
            writerThread.start();

            this.workerThreads.add(writerThread);
        }

        this.startUpLatch.countDown();
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
    public void checkTimingMap(final boolean isToForce) {

        if (isToForce ||
                (this.timingMap == null)) {

            final Map<Long, List<ReaderWriterBase.SampleEntry<T>>> tempTimingMap = new TreeMap<>();

            for (final List<ReaderWriterBase.ReaderWriterWorker<T>> listItem :
                    Arrays.asList(this.writerWorkers, this.readerWorkers)) {

                for (final ReaderWriterBase.ReaderWriterWorker<T> workerItem : listItem) {

                    for (final ReaderWriterBase.SampleEntry<T> sampleItem : workerItem.getSampleLog()) {

                        List<ReaderWriterBase.SampleEntry<T>> entries = tempTimingMap.get(sampleItem.getTime());

                        if (entries == null) {

                            entries = new ArrayList<>();
                            tempTimingMap.put(sampleItem.getTime(), entries);
                        }

                        entries.add(sampleItem);
                    }
                }
            }

            this.timingMap = tempTimingMap;
        }
    }

    /**
     * Check order.
     *
     * @return True if reader/writer logs out of order, false otherwise.
     */

    public boolean checkOrder() {

        boolean result = false;
        this.checkTimingMap(false);

        T lastReaderValue = null;
        long lastReaderTime = 0L;

        T lastWriterValue = null;
        long lastWriterTime = 0L;

        for (final Map.Entry<Long, List<ReaderWriterBase.SampleEntry<T>>> entryItem : this.timingMap.entrySet()) {

            for (final ReaderWriterBase.SampleEntry<T> logItem : entryItem.getValue()) {

                if (logItem.isReader()) {

                    lastReaderValue = logItem.getData();
                    lastReaderTime = logItem.getTime();

                    if (lastReaderTime > lastWriterTime) {

                        if (((lastReaderValue != null) && (lastWriterValue != null)) &&
                                (lastReaderValue.compareTo(lastWriterValue) < 0)) {

                            result = true;
                            break;
                        }
                    }

                } else {

                    lastWriterValue = logItem.getData();
                    lastWriterTime = logItem.getTime();
                }
            }

            if (result) {

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

        final int totalSamples = (readerSamples + writerSamples);
        System.out.println(String.format("\nLog samples - reader=%d, writer=%d, total=%d.",
                readerSamples, writerSamples, totalSamples));

        this.checkTimingMap(false);

        T lastReaderValue = null;
        long lastReaderTime = 0L;

        T lastWriterValue = null;
        long lastWriterTime = 0L;

        final Deque<String> lines = new ArrayDeque<>();
        int sampleCtr = 0;

        boolean isOutOfOrder = false;

        for (final Map.Entry<Long, List<ReaderWriterBase.SampleEntry<T>>> entryItem : this.timingMap.entrySet()) {

            for (final ReaderWriterBase.SampleEntry<T> logItem : entryItem.getValue()) {

                String flagText = "";

                if (logItem.isReader()) {

                    lastReaderValue = logItem.getData();
                    lastReaderTime = logItem.getTime();

                    if (lastReaderTime > lastWriterTime) {

                        if (((lastReaderValue != null) && (lastWriterValue != null)) &&
                                (lastReaderValue.compareTo(lastWriterValue) < 0)) {

                            isOutOfOrder = true;
                            flagText = " / ***";
                        }
                    }

                } else {

                    lastWriterValue = logItem.getData();
                    lastWriterTime = logItem.getTime();
                }

                final String line = String.format("%s / %s / %s%s",
                        (logItem.isReader() ? "READER" : "WRITER"),
                        String.valueOf(logItem.getTime()),
                        String.valueOf(logItem.getData()),
                        flagText);
                lines.addLast(line);

                while (lines.size() > 10) {

                    lines.removeFirst();
                }

                sampleCtr++;

                if (isOutOfOrder) {

                    System.out.println(String.format("\nEntry #%d:\n", sampleCtr));

                    for (final String item : lines) {

                        System.out.println(item);
                    }

                    System.out.println(String.format("\n((Skipping %d samples))",
                            (totalSamples - sampleCtr)));
                    break;
                }
            }

            if (isOutOfOrder) {

                break;
            }
        }
    }

    /**
     * Base worker class.
     */
    public abstract static class ReaderWriterWorker<T> implements Runnable {

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
         * Log lock.
         */
        private ReentrantLock logLock;

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

                    this.runImpl();
                    Thread.sleep(1L);
                }

            } catch (final InterruptedException ex) {

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
         * @return True if logged, false otherwise.
         */
        public boolean logSample(final T data,
                                 final long time) {

            this.logLock.lock();

            try {

                this.prevData = data;
                this.sampleLog.add(new ReaderWriterBase.SampleEntry<>(this.isReader, data,
                        ((time < 1L) ? System.nanoTime() : time)));

            } finally {

                this.logLock.unlock();
            }

            return true;
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
         * Sets log lock.
         *
         * @param logLock Log lock.
         */
        public void setLogLock(final ReentrantLock logLock) {

            this.logLock = logLock;
        }
    }

    /**
     * Timing entry.
     */
    public static class SampleEntry<T> {

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
