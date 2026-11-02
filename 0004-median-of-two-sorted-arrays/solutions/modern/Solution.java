class Solution {
    private static final int MIN = Integer.MIN_VALUE;
    private static final int MAX = Integer.MAX_VALUE;

    public double findMedianSortedArrays(int[] a, int[] b) {
        if (a.length > b.length) {
            int[] t = a; a = b; b = t;
        }
        int m = a.length, n = b.length;
        int half = (m + n + 1) >>> 1;
        int lo = 0, hi = m;

        while (lo <= hi) {
            int cutA = (lo + hi) >>> 1;
            int cutB = half - cutA;

            int aLeft = cutA == 0 ? MIN : a[cutA - 1];
            int aRight = cutA == m ? MAX : a[cutA];
            int bLeft = cutB == 0 ? MIN : b[cutB - 1];
            int bRight = cutB == n ? MAX : b[cutB];

            if (aLeft > bRight) {
                hi = cutA - 1;
            } else if (bLeft > aRight) {
                lo = cutA + 1;
            } else {
                int leftMax = Math.max(aLeft, bLeft);
                if (((m + n) & 1) == 1) {
                    return leftMax;
                }
                int rightMin = Math.min(aRight, bRight);
                return ((long) leftMax + rightMin) / 2.0;
            }
        }
        throw new IllegalArgumentException("not sorted");
    }
}
