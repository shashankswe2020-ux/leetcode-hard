class Solution {
    public double findMedianSortedArrays(int[] a, int[] b) {
        int total = a.length + b.length;
        if (total % 2 == 1) {
            return kth(a, b, total / 2 + 1);
        }
        long left = kth(a, b, total / 2);
        long right = kth(a, b, total / 2 + 1);
        return (left + right) / 2.0;
    }

    // k-th smallest (1-based) of a and b together
    private int kth(int[] a, int[] b, int k) {
        int i = 0, j = 0;
        while (true) {
            if (i == a.length) return b[j + k - 1];
            if (j == b.length) return a[i + k - 1];
            if (k == 1) return Math.min(a[i], b[j]);

            int step = k / 2;
            int lastA = Math.min(i + step, a.length) - 1;
            int lastB = Math.min(j + step, b.length) - 1;
            if (a[lastA] <= b[lastB]) {
                k -= lastA - i + 1;
                i = lastA + 1;
            } else {
                k -= lastB - j + 1;
                j = lastB + 1;
            }
        }
    }
}
