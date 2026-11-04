class Solution {
    public int trap(int[] bars) {
        int n = bars.length;
        int water = 0;
        for (int i = 0; i < n; i++) {
            int maxLeft = 0, maxRight = 0;
            for (int j = 0; j <= i; j++) {
                maxLeft = Math.max(maxLeft, bars[j]);
            }
            for (int j = i; j < n; j++) {
                maxRight = Math.max(maxRight, bars[j]);
            }
            int level = Math.min(maxLeft, maxRight);
            water += level - bars[i];
        }
        return water;
    }
}
