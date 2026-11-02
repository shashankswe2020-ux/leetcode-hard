import java.util.List;
import java.util.function.LongToIntFunction;

// Median of data split across many machines.
// A shard answers one thing: "how many are <= v?"
class ShardedMedian {
    static double median(List<LongToIntFunction> shards,
                         long total) {
        long left = kth(shards, (total + 1) / 2);
        if (total % 2 == 1) {
            return left;
        }
        long right = kth(shards, total / 2 + 1);
        return (left + right) / 2.0;
    }

    // one round = one count request to every shard
    static long kth(List<LongToIntFunction> shards,
                    long k) {
        long lo = Integer.MIN_VALUE, hi = Integer.MAX_VALUE;
        while (lo < hi) {
            long mid = Math.floorDiv(lo + hi, 2);
            long count = 0;
            for (var shard : shards) {
                count += shard.applyAsInt(mid);
            }
            if (count >= k) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }
}
