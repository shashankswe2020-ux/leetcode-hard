class Solution {
    public int splitArray(int[] nums, int k) {
        long lo = 0, hi = 0;
        for (int num : nums) {
            lo = Math.max(lo, num);
            hi += num;
        }
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (pieces(nums, mid) <= k) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return Math.toIntExact(lo);
    }

    // fewest pieces when no piece may weigh more than limit
    private int pieces(int[] nums, long limit) {
        int count = 1;
        long sum = 0;
        for (int num : nums) {
            if (sum + num > limit) {
                count++;
                sum = num;
            } else {
                sum += num;
            }
        }
        return count;
    }
}
