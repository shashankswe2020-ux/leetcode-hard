import java.util.Arrays;

// Follow-up: the numbers are doubles. Halving [max, total] by value
// never lands exactly. But the answer is always the weight of some
// piece, so search the sorted list of every run's weight instead.
// Each run is added left to right, exactly the way the greedy
// check adds a piece, so a candidate is never rounded differently
// from the piece it names. Cost: n (n + 1) / 2 candidates in memory
// and an n^2 log n sort, before about log2 of that many checks.
final class DoubleCandidates {

    static double splitArray(double[] nums, int k) {
        int n = nums.length;
        double[] runs = new double[n * (n + 1) / 2];
        int r = 0;
        double biggest = 0;
        for (int i = 0; i < n; i++) {
            biggest = Math.max(biggest, nums[i]);
            double s = 0;
            for (int j = i; j < n; j++) {
                s += nums[j];
                runs[r++] = s;
            }
        }
        Arrays.sort(runs);
        int lo = 0, hi = runs.length - 1;
        while (runs[lo] < biggest) lo++;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (pieces(nums, runs[mid]) <= k) hi = mid; else lo = mid + 1;
        }
        return runs[lo];
    }

    static int pieces(double[] nums, double limit) {
        int count = 1;
        double sum = 0;
        for (double num : nums) {
            if (sum + num > limit) { count++; sum = num; }
            else sum += num;
        }
        return count;
    }

    // reference: every cut, each piece added left to right
    static double reference(double[] a, int k) {
        return best(a, 0, k);
    }

    private static double best(double[] a, int from, int k) {
        int n = a.length;
        if (k == 1) {
            double s = 0;
            for (int i = from; i < n; i++) s += a[i];
            return s;
        }
        double bestW = Double.POSITIVE_INFINITY, s = 0;
        for (int end = from; end <= n - k; end++) {
            s += a[end];
            bestW = Math.min(bestW, Math.max(s, best(a, end + 1, k - 1)));
        }
        return bestW;
    }
}
