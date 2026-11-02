// Homework answer: the k-th smallest of two sorted arrays, using the cut.
// Same search as the median, but the left side holds k numbers instead of half.
class KthSmallest {
    static int kth(int[] a, int[] b, int k) {          // k is 1-based
        if (a.length > b.length) return kth(b, a, k);
        int m = a.length, n = b.length;
        int lo = Math.max(0, k - n), hi = Math.min(m, k);
        while (lo <= hi) {
            int cutA = (lo + hi) >>> 1, cutB = k - cutA;
            int aLeft = cutA == 0 ? Integer.MIN_VALUE : a[cutA - 1];
            int aRight = cutA == m ? Integer.MAX_VALUE : a[cutA];
            int bLeft = cutB == 0 ? Integer.MIN_VALUE : b[cutB - 1];
            int bRight = cutB == n ? Integer.MAX_VALUE : b[cutB];
            if (aLeft > bRight) hi = cutA - 1;
            else if (bLeft > aRight) lo = cutA + 1;
            else return Math.max(aLeft, bLeft);
        }
        throw new IllegalArgumentException("k out of range");
    }
}
