// Homework answer: LeetCode 875, Koko Eating Bananas.
// Same pattern: guess the speed, check it in one pass,
// binary search the smallest speed that is fast enough.
class Solution {
    public int minEatingSpeed(int[] piles, int h) {
        int lo = 1, hi = 0;
        for (int pile : piles) {
            hi = Math.max(hi, pile);
        }
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (hours(piles, mid) <= h) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    // hours at this speed; long: 10^4 piles of 10^9
    // at speed 1 is 10^13 hours, far past an int
    private long hours(int[] piles, int speed) {
        long total = 0;
        for (int pile : piles) {
            total += (pile - 1) / speed + 1;
        }
        return total;
    }
}
