import java.util.Arrays;

// The candidate search on ints (the doubles version is
// DoubleCandidates): the answer is always the weight of some
// piece, so binary search the sorted list of all run sums:
// n (n + 1) / 2 candidates in memory, an n^2 log n sort,
// then about log2 of that many guesses, each the same check.
final class CandidateSearch {

    static long splitArray(int[] nums, int k, int[] probes) {
        int n = nums.length;
        long[] prefix = new long[n + 1];
        long biggest = 0;
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
            biggest = Math.max(biggest, nums[i]);
        }
        long[] runs = new long[n * (n + 1) / 2];
        int r = 0;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j <= n; j++) runs[r++] = prefix[j] - prefix[i];
        Arrays.sort(runs);
        // same low end as before: nothing below the biggest number
        int lo = 0, hi = runs.length - 1;    // the total always fits
        while (runs[lo] < biggest) lo++;
        probes[0] = 0;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            probes[0]++;
            if (Variants.count(nums, runs[mid]) <= k) hi = mid; else lo = mid + 1;
        }
        return runs[lo];
    }
}
