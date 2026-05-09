import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Task 3 - thread-safety in BankStatementBatchProcessor
 *
 * Bug: processedCount at the end of a run keeps coming out lower than the
 * number of records we actually processed.
 *
 * Rule: only touch the counter. Don't mess with the executor, pool size or processRecord().
 */
public class Task3 {

    public static class BankStatementBatchProcessor {

        // FIX: `processedCount++` looks like one thing but it's really three
        // (read, add 1, write back). With 10 threads running, two of them
        // can read the same value and both write back the same +1, so we
        // lose increments. AtomicInteger does the increment in one atomic
        // step and also handles cross-thread visibility - no synchronized
        // block needed.
        private final AtomicInteger processedCount = new AtomicInteger(0);

        public void process(List<StatementRecord> records) throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (StatementRecord record : records) {
                executor.submit(() -> {
                    processRecord(record);
                    // FIX: was processedCount++ - that's the race. swap it
                    // for incrementAndGet() and the incorrect count(lost-update) problem goes away.
                    processedCount.incrementAndGet();
                });
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
        }

        public int getProcessedCount() {
            // .get() makes sure we see whatever the worker threads last wrote.
            return processedCount.get();
        }

        private void processRecord(StatementRecord record) {
            // left alone on purpose - out of scope per the task rules
        }
    }

    static class StatementRecord { }
}
