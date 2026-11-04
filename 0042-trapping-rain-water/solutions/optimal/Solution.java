class Solution {
    public int trap(int[] bars) {
        int left = 0, right = bars.length - 1;
        int maxLeft = 0, maxRight = 0;
        int water = 0;

        while (left < right) {
            if (bars[left] < bars[right]) {
                maxLeft = Math.max(maxLeft, bars[left]);
                water += maxLeft - bars[left];
                left++;
            } else {
                maxRight = Math.max(maxRight, bars[right]);
                water += maxRight - bars[right];
                right--;
            }
        }
        return water;
    }
}
