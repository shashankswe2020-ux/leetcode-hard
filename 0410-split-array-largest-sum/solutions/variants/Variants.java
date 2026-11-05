import java.util.Arrays;

// Candidates for the modern Java chapter, measured in Harness.
final class Variants {

    // setup with a plain loop
    static long[] setupLoop(int[] nums) {
        long lo = 0, hi = 0;
        for (int num : nums) {
            lo = Math.max(lo, num);
            hi += num;
        }
        return new long[] {lo, hi};
    }

    // setup with streams (reads nicely, two passes)
    static long[] setupStream(int[] nums) {
        long lo = Arrays.stream(nums).max().orElse(0);
        long hi = Arrays.stream(nums).asLongStream().sum();
        return new long[] {lo, hi};
    }

    // probes the binary search makes from [lo, hi]
    static int probes(int[] nums, int k, boolean averageBound) {
        long[] b = setupLoop(nums);
        long lo = b[0], hi = b[1];
        if (averageBound) lo = Math.max(lo, Math.ceilDiv(hi, k));
        int probes = 0;
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            probes++;
            if (count(nums, mid) <= k) hi = mid; else lo = mid + 1;
        }
        return probes;
    }

    static int count(int[] nums, long limit) {
        int count = 1;
        long sum = 0;
        for (int num : nums) {
            if (sum + num > limit) { count++; sum = num; }
            else sum += num;
        }
        return count;
    }

    // the optimal loop with long sums but no average bound
    // and no early exit (isolates each tip's effect)
    static int longOnly(int[] nums, int k) {
        long[] b = setupLoop(nums);
        long lo = b[0], hi = b[1];
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (count(nums, mid) <= k) hi = mid; else lo = mid + 1;
        }
        return Math.toIntExact(lo);
    }
}
