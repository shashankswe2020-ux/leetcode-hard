class Solution {
    public int trap(int[] bars) {
        int n = bars.length;
        int[] left = new int[n];    // tallest in 0..i
        int[] right = new int[n];   // tallest in i..n-1

        left[0] = bars[0];
        for (int i = 1; i < n; i++) {
            left[i] = Math.max(left[i - 1], bars[i]);
        }
        right[n - 1] = bars[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            right[i] = Math.max(right[i + 1], bars[i]);
        }

        int water = 0;
        for (int i = 0; i < n; i++) {
            int level = Math.min(left[i], right[i]);
            water += level - bars[i];
        }
        return water;
    }
}
