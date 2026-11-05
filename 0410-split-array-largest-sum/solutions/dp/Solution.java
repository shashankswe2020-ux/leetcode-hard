class Solution {
    public int splitArray(int[] nums, int k) {
        int n = nums.length;
        int[] prefix = new int[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        // best[j][i]: first i numbers in j pieces
        int[][] best = new int[k + 1][];
        best[1] = prefix.clone();
        for (int j = 2; j <= k; j++) {
            int[] prev = best[j - 1];
            best[j] = new int[n + 1];
            for (int i = j; i <= n; i++) {
                int low = Integer.MAX_VALUE;
                for (int cut = j - 1; cut < i; cut++) {
                    int last = prefix[i] - prefix[cut];
                    int worst = Math.max(prev[cut], last);
                    low = Math.min(low, worst);
                }
                best[j][i] = low;
            }
        }
        return best[k][n];
    }
}
