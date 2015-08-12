package com.opsysinc.learning.cert.examples.util;


import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Reader/writer base class.
 * <p/>
 * Created by Michael J. Kitchin on 8/12/2015.
 */
public class ReaderWriterBase<T extends Comparable> {

    /**
     * Reader log.
     */
    private final List<ReaderWriterBase.TimingEntry<T>> readerLog;

    /**
     * Writer log.
     */
    private final List<ReaderWriterBase.TimingEntry<T>> writerLog;

    /**
     * Last reader data.
     */
    private T lastReaderData;

    /**
     * Last writer data.
     */
    private T lastWriterData;

    /**
     * Reader thread.
     */
    private Thread readerThread;

    /**
     * Writer thread.
     */
    private Thread writerThread;

    /**
     * Start up latch.
     */
    private final CountDownLatch startUpLatch;

    /**
     * Basic ctor.
     */
    public ReaderWriterBase() {

        this.readerLog = new LinkedList<>();
        this.writerLog = new LinkedList<>();
        this.startUpLatch = new CountDownLatch(3);
    }


    /**
     * Start up.
     *
     * @param readerRunnable Reader.
     * @param writerRunnable Writer.
     */
    public void startUp(final Runnable readerRunnable,
                        final Runnable writerRunnable) {

        this.readerThread = new Thread(readerRunnable);

        this.readerThread.setDaemon(true);
        this.readerThread.start();

        this.writerThread = new Thread(writerRunnable);

        this.writerThread.setDaemon(true);
        this.writerThread.start();

        this.startUpLatch.countDown();
    }

    /**
     * Clean up.
     */
    public void cleanUp() throws InterruptedException {

        this.readerThread.interrupt();
        this.writerThread.interrupt();

        this.readerThread.join();
        this.writerThread.join();

        this.readerThread = null;
        this.writerThread = null;
    }

    /**
     * Gets startup latch.
     *
     * @return Startup latch.
     */
    public CountDownLatch getStartUpLatch() {

        return this.startUpLatch;
    }

    /**
     * Log for reader.
     *
     * @param readerData Reader data.
     * @param sampleTime Sample time.
     * @return True if logged, false otherwise.
     */
    public boolean logReader(final T readerData,
                             final long sampleTime) {

        final boolean result = false;

        if (readerData != this.lastReaderData) {

            if ((readerData == null) || (this.lastReaderData == null) ||
                    !readerData.equals(this.lastReaderData)) {

                this.lastReaderData = readerData;
                this.readerLog.add(new ReaderWriterBase.TimingEntry<T>(
                        true, readerData, sampleTime));
            }
        }

        return result;
    }

    /**
     * Log for writer.
     *
     * @param writerData Writer data.
     * @param sampleTime Sample time.
     * @return True if logged, false otherwise.
     */
    public boolean logWriter(final T writerData,
                             final long sampleTime) {

        final boolean result = false;

        if (writerData != this.lastWriterData) {

            if ((writerData == null) || (this.lastWriterData == null) ||
                    !writerData.equals(this.lastWriterData)) {

                this.lastWriterData = writerData;
                this.writerLog.add(new ReaderWriterBase.TimingEntry<T>(
                        false, writerData, sampleTime));
            }
        }

        return result;
    }

    /**
     * Build timing map.
     *
     * @return Timing map.
     */
    public Map<Long, List<ReaderWriterBase.TimingEntry<T>>> buildTimingMap() {

        final Map<Long, List<ReaderWriterBase.TimingEntry<T>>> result = new TreeMap<>();

        for (final ReaderWriterBase.TimingEntry<T> item : this.writerLog) {

            List<ReaderWriterBase.TimingEntry<T>> entries = result.get(item.getTime());

            if (entries == null) {

                entries = new ArrayList<>();
                result.put(item.getTime(), entries);
            }

            entries.add(item);
        }

        for (final ReaderWriterBase.TimingEntry<T> item : this.readerLog) {

            List<ReaderWriterBase.TimingEntry<T>> entries = result.get(item.getTime());

            if (entries == null) {

                entries = new ArrayList<>();
                result.put(item.getTime(), entries);
            }

            entries.add(item);
        }

        return result;
    }

    /**
     * Check order.
     *
     * @return True if reader/writer logs out of order, false otherwise.
     */
    public boolean checkOrder() {

        boolean result = false;
        final Map<Long, List<ReaderWriterBase.TimingEntry<T>>> timingMap = this.buildTimingMap();

        T lastReaderValue = null;
        long lastReaderTime = 0L;

        T lastWriterValue = null;
        long lastWriterTime = 0L;

        for (final Map.Entry<Long, List<ReaderWriterBase.TimingEntry<T>>> entryItem : timingMap.entrySet()) {

            for (final ReaderWriterBase.TimingEntry<T> logItem : entryItem.getValue()) {

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

        final int totalSamples = (this.readerLog.size() + this.writerLog.size());
        System.out.println(String.format("\nLog samples - reader=%d, writer=%d, total=%d.",
                this.readerLog.size(), this.writerLog.size(), totalSamples));

        final Map<Long, List<ReaderWriterBase.TimingEntry<T>>> timingMap = this.buildTimingMap();

        T lastReaderValue = null;
        long lastReaderTime = 0L;

        T lastWriterValue = null;
        long lastWriterTime = 0L;

        final Deque<String> lines = new ArrayDeque<>();
        int entryCtr = 0;

        boolean isOutOfOrder = false;

        for (final Map.Entry<Long, List<ReaderWriterBase.TimingEntry<T>>> entryItem : timingMap.entrySet()) {

            for (final ReaderWriterBase.TimingEntry<T> logItem : entryItem.getValue()) {

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

                while (lines.size() > 5) {

                    lines.removeFirst();
                }

                entryCtr++;

                if (isOutOfOrder) {

                    System.out.println(String.format("\nEntry #%d:\n", entryCtr));

                    for (final String item : lines) {

                        System.out.println(item);
                    }

                    isOutOfOrder = false;
                }
            }
        }
    }

    /**
     * Base worker class.
     */
    public abstract class WorkerBase implements Runnable {

        @Override
        public void run() {

            try {

                ReaderWriterBase.this.startUpLatch.countDown();
                ReaderWriterBase.this.startUpLatch.await();

                while (!Thread.interrupted()) {

                    this.runImpl();
                    final long sleepTimeInMs = this.sleepTimeInMs();

                    if (sleepTimeInMs > 0L) {

                        Thread.sleep(sleepTimeInMs);
                    }
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
         * Sleep time.
         *
         * @return Sleep time.
         */
        protected abstract long sleepTimeInMs();
    }

    /**
     * Timing entry.
     */
    public static class TimingEntry<T> {

        /**
         * Is reader.
         */
        private final boolean isReader;

        /**
         * Data.
         */
        private final T data;

        /**
         * Time.
         */
        private final long time;

        /**
         * Basic ctor.
         *
         * @param isReader   True if reader, false otherwise.
         * @param data       Payload.
         * @param sampleTime Sample time.
         */
        public TimingEntry(final boolean isReader,
                           final T data,
                           final long sampleTime) {

            this.isReader = isReader;
            this.data = data;
            this.time = sampleTime;
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
         * Gets reader.
         *
         * @return Reader.
         */
        public boolean isReader() {

            return this.isReader;
        }
    }

}
