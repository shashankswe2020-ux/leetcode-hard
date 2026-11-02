class Solution {
    public double findMedianSortedArrays(int[] a, int[] b) {
        int total = a.length + b.length;
        long left = kth(a, b, (total + 1) / 2);
        if (total % 2 == 1) {
            return left;
        }
        long right = kth(a, b, total / 2 + 1);
        return (left + right) / 2.0;
    }

    // smallest value v with at least k numbers <= v
    private long kth(int[] a, int[] b, int k) {
        long lo = Integer.MIN_VALUE, hi = Integer.MAX_VALUE;
        while (lo < hi) {
            long mid = Math.floorDiv(lo + hi, 2);
            int count = atMost(a, mid) + atMost(b, mid);
            if (count >= k) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    // how many numbers in sorted x are <= v
    private int atMost(int[] x, long v) {
        int lo = 0, hi = x.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (x[mid] <= v) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }
}
