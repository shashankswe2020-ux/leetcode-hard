import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// The coordinator's two rounds, one remote call per shard.
// Pass a virtual-thread executor (Java 21): one cheap
// thread per call, so all calls wait at the same time.
class FanOut {
    interface Shard {
        int max();                              // round 1
        long fill(int wallLeft, int wallRight); // round 2
    }

    // The coordinator: one virtual thread per remote call.
    static long trap(List<Shard> shards) throws Exception {
        ExecutorService pool =
                Executors.newVirtualThreadPerTaskExecutor();
        try (pool) {
            return trap(shards, pool);
        }
    }

    static long trap(List<Shard> shards,
            ExecutorService pool) throws Exception {
        int k = shards.size();
        List<Future<Integer>> calls = new ArrayList<>();
        for (Shard s : shards) {
            calls.add(pool.submit(s::max));
        }
        int[] left = new int[k], right = new int[k];
        for (int s = 1; s < k; s++) {
            int m = calls.get(s - 1).get();
            left[s] = Math.max(left[s - 1], m);
        }
        for (int s = k - 2; s >= 0; s--) {
            int m = calls.get(s + 1).get();
            right[s] = Math.max(right[s + 1], m);
        }
        List<Future<Long>> fills = new ArrayList<>();
        for (int s = 0; s < k; s++) {
            Shard shard = shards.get(s);
            int l = left[s], r = right[s];
            fills.add(pool.submit(() -> shard.fill(l, r)));
        }
        long water = 0;
        for (Future<Long> f : fills) {
            water += f.get();
        }
        return water;
    }
}
