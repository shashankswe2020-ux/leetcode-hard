class Solution {
    public int splitArray(int[] nums, int k) {
        int n = nums.length;
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        int[] best = prefix.clone();     // one piece
        for (int j = 2; j <= k; j++) {
            int[] next = new int[n + 1];
            for (int i = j; i <= n; i++) {
                int low = Integer.MAX_VALUE;
                // stop once the last piece alone is too big
                for (int cut = i - 1; cut >= j - 1; cut--) {
                    int last = prefix[i] - prefix[cut];
                    if (last >= low) break;
                    int worst = Math.max(best[cut], last);
                    low = Math.min(low, worst);
                }
                next[i] = low;
            }
            best = next;
        }
        return best[n];
    }
}
