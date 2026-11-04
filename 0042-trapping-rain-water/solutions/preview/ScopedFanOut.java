import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.atomic.AtomicInteger;

// Java 25 preview: structured concurrency.
// Compile and run with --enable-preview.
// Round one as one scope: if any shard call fails, the
// scope cancels the others instead of leaving them hanging.
public class ScopedFanOut {
    static int[] maxima(List<FanOut.Shard> shards)
            throws InterruptedException {
        try (var scope = StructuredTaskScope.open()) {
            var calls = new ArrayList<Subtask<Integer>>();
            for (FanOut.Shard s : shards) {
                calls.add(scope.fork(s::max));
            }
            scope.join();     // throws if any call failed
            int[] max = new int[calls.size()];
            for (int i = 0; i < max.length; i++) {
                max[i] = calls.get(i).get();
            }
            return max;
        }
    }

    // Test: 1,000 shards x 1 s; shard 7 fails at 5 ms.
    public static void main(String[] args)
            throws Exception {
        AtomicInteger finished = new AtomicInteger();
        List<FanOut.Shard> shards = new ArrayList<>();
        for (int s = 0; s < 1000; s++) {
            shards.add(slowShard(s, finished));
        }
        long t0 = System.nanoTime();
        String outcome = "no failure seen";
        try {
            maxima(shards);
        } catch (StructuredTaskScope.FailedException e) {
            String why = e.getCause().getMessage();
            outcome = "failed fast: " + why;
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("structured, 1000 x 1 s,"
                + " one fails at 5 ms: " + outcome
                + " after " + ms + " ms; calls completed: "
                + finished.get());
        if (ms > 500 || finished.get() > 0) {
            System.exit(1);
        }
    }

    static FanOut.Shard slowShard(int id,
            AtomicInteger done) {
        return new FanOut.Shard() {
            public int max() {
                sleep(id == 7 ? 5 : 1000);
                if (id == 7) {
                    throw new IllegalStateException(
                            "shard 7");
                }
                done.incrementAndGet();
                return id;
            }

            public long fill(int wallLeft, int wallRight) {
                return 0;
            }
        };
    }

    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
