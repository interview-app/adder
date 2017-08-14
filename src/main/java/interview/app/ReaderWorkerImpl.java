package interview.app;

import java.io.RandomAccessFile;

class ReaderWorkerImpl implements ReaderWorker {
    private final int maxChuckSize;
    private final long from;
    private final long to;
    private final String path;
    private final CalculationManager calculationManager;

    public ReaderWorkerImpl(String path, long from, long to, int maxChuckSize, CalculationManager calculationManager) {
        this.path = path;
        this.from = from;
        this.to = to;
        this.maxChuckSize = maxChuckSize;
        this.calculationManager = calculationManager;
    }

    @Override
    public void run() {
        try {
            RandomAccessFile f = new RandomAccessFile(path, "r");
            f.seek(from);

            long len = to - from;
            long fullN = len / maxChuckSize;
            long left = len % maxChuckSize;

            for (long i = 0; i < fullN; ++i) {
                byte[] fullChunk = new byte[maxChuckSize];
                f.readFully(fullChunk);
                calculationManager.addChunk(fullChunk);

                if (Thread.interrupted()) {
                    return;
                }
            }

            if (left > 0) {
                byte[] leftChunk = new byte[(int) left];
                f.readFully(leftChunk);
                calculationManager.addChunk(leftChunk);
            }

            calculationManager.readerFinished(this);
        } catch (Exception e) {
            calculationManager.error(e);
        }
    }

    @Override
    public String toString() {
        return "interview.app.ReaderWorker{file[" + path + " ] with partition[from:" + from + ", to:" + to + "]}";
    }
}

