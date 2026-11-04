// Homework answer: LeetCode 11, Container With Most Water.
// Same two pointers, different question: two lines and
// the x-axis hold the water; bars in between don't matter.
// Move the shorter line: moving the taller one only
// shrinks the width and never raises the min: it can't win.
class Solution {
    public int maxArea(int[] bars) {
        int left = 0, right = bars.length - 1;
        int best = 0;
        while (left < right) {
            int h = Math.min(bars[left], bars[right]);
            best = Math.max(best, h * (right - left));
            if (bars[left] < bars[right]) {
                left++;
            } else {
                right--;
            }
        }
        return best;
    }
}
