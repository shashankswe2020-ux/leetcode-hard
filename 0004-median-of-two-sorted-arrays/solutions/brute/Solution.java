import java.util.Arrays;

class Solution {
    public double findMedianSortedArrays(int[] a, int[] b) {
        int[] all = new int[a.length + b.length];
        System.arraycopy(a, 0, all, 0, a.length);
        System.arraycopy(b, 0, all, a.length, b.length);
        Arrays.sort(all);

        int mid = all.length / 2;
        if (all.length % 2 == 1) {
            return all[mid];
        }
        return (all[mid - 1] + all[mid]) / 2.0;
    }
}
