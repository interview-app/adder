package interview.app;

class AdderWorkerImpl implements AdderWorker {

    private long total;
    private final CalculationManager calculationManager;

    public AdderWorkerImpl(CalculationManager calculationManager) {
        this.calculationManager = calculationManager;
    }

    @Override
    public void run() {
        try {
            while (calculationManager.hasMoreWorkForAdder() && !Thread.interrupted()) {
                byte[] batch = calculationManager.nextChunk();

                if (batch != null) {
                    for (int i = 0; i < batch.length; i += 4) {
                        total = CalculationUtil.sumUnsignedLongWithLittleEndianInt(total,
                                batch[i],
                                batch[i + 1],
                                batch[i + 2],
                                batch[i + 3]
                        );
                    }
                }
            }

            calculationManager.adderFinished(this);
        } catch (Exception e) {
            calculationManager.error(e);
        }
    }

    @Override
    public long getTotal() {
        return total;
    }

}