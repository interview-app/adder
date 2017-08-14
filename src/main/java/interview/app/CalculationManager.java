package interview.app;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

class CalculationManager {
    private static final int DEFAULT_NUMBER_SIZE = 4;
    private static final int DEFAULT_QUEUE_SIZE = 50;
    //align to block sizes of 4096 or 8192
    private static final int DEFAULT_MAX_CHUNK_SIZE = 8192 * 640;//5242880;//5mb

    private long total;
    private final String filePath;

    private final BlockingQueue<byte[]> q = new LinkedBlockingQueue<>(DEFAULT_QUEUE_SIZE);

    private final int readersNumber;
    private final int addersNumber;

    private final Set<ReaderWorker> readers = new HashSet<>();
    private final Set<AdderWorker> adders = new HashSet<>();

    private ExecutorService readersPool;
    private ExecutorService addersPool;

    private final CountDownLatch readersLatch;
    private final CountDownLatch addersLatch;

    private Exception error;

    private CalculationManager(String filePath, int readersNumber, int addersNumber) {
        this.filePath = filePath;
        this.readersNumber = readersNumber;
        this.addersNumber = addersNumber;
        this.readersLatch = new CountDownLatch(this.readersNumber);
        this.addersLatch = new CountDownLatch(this.addersNumber);
    }

    public synchronized void readerFinished(ReaderWorker r) {
        if (readers.contains(r)) {
            readers.remove(r);
            readersLatch.countDown();
        } else {
            error(new UnauthorizedNotificationException("Notification from unknown reader: " + r));
        }
    }

    public synchronized void adderFinished(AdderWorker a) {
        if (adders.contains(a)) {
            adders.remove(a);
            addersLatch.countDown();
            try {
                this.total = CalculationUtil.sumUnsigned(total, a.getTotal());
            } catch (Exception e) {
                error(e);
            }
        } else {
            error(new UnauthorizedNotificationException("Notification from unknown adder: " + a));
        }
    }

    public boolean hasMoreWorkForAdder() {
        return readersLatch.getCount() != 0 || !q.isEmpty();
    }

    public void addChunk(byte[] chunk) {
        try {
            q.put(chunk);
        } catch (Exception e) {
            error(e);
        }
    }

    public byte[] nextChunk() {
        try {
            return q.poll(100, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            error(e);
        }
        return null;
    }

    public void error(Exception e) {
        this.error = e;
        while (readersLatch.getCount() > 0) {
            readersLatch.countDown();
        }
        while (addersLatch.getCount() > 0) {
            addersLatch.countDown();
        }
    }

    private String getTotal() {
        return Long.toUnsignedString(total);
    }

    private void awaitCompletion() throws InterruptedException {
        readersLatch.await();
        addersLatch.await();
    }

    private String calc() throws Exception {
        long len = Files.size(Paths.get(filePath));

        long partitionSize = len / readersNumber;
        //should be aligned to do not split numbers in different partitions
        partitionSize -= partitionSize % DEFAULT_NUMBER_SIZE;

        readersPool = Executors.newFixedThreadPool(readersNumber);
        addersPool = Executors.newFixedThreadPool(addersNumber);

        for (int i = 0; i < addersNumber; ++i) {
            AdderWorkerImpl a = new AdderWorkerImpl(this);
            adders.add(a);
            addersPool.execute(a);
        }

        long from = 0;
        for (int i = 0; i < readersNumber; ++i) {
            long to = from + partitionSize;
            if (i == readersNumber - 1) {
                to = len;
            }
            ReaderWorker r = new ReaderWorkerImpl(filePath, from, to, DEFAULT_MAX_CHUNK_SIZE,this);
            readers.add(r);
            readersPool.execute(r);
            from = to;
        }

        awaitCompletion();

        readersPool.shutdown();
        addersPool.shutdown();

        if (error != null) {
            throw error;
        }

        return getTotal();
    }

    public static String calculate(String filePath, int readersNumber, int addersNumber) throws Exception {
        return new CalculationManager(filePath, readersNumber, addersNumber).calc();
    }
}