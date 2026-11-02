class Solution {
    public double findMedianSortedArrays(int[] a, int[] b) {
        int total = a.length + b.length;
        int i = 0, j = 0;
        int prev = 0, curr = 0;

        for (int step = 0; step <= total / 2; step++) {
            prev = curr;
            boolean takeA = j == b.length
                    || (i < a.length && a[i] <= b[j]);
            curr = takeA ? a[i++] : b[j++];
        }

        if (total % 2 == 1) {
            return curr;
        }
        return (prev + curr) / 2.0;
    }
}
