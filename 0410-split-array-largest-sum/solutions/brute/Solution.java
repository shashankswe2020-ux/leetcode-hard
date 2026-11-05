class Solution {
    public int splitArray(int[] nums, int k) {
        return best(nums, 0, k);
    }

    // heaviest piece of the best split of
    // nums[start..] into k pieces
    private int best(int[] nums, int start, int k) {
        if (k == 1) {
            int sum = 0;
            for (int i = start; i < nums.length; i++) {
                sum += nums[i];
            }
            return sum;
        }
        int answer = Integer.MAX_VALUE;
        int first = 0;
        // leave at least one number for every later piece
        int lastEnd = nums.length - k;
        for (int end = start; end <= lastEnd; end++) {
            first += nums[end];
            int rest = best(nums, end + 1, k - 1);
            int worst = Math.max(first, rest);
            answer = Math.min(answer, worst);
        }
        return answer;
    }
}
