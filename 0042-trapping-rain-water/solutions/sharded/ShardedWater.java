import java.util.List;

// Bars split in order across machines. Two rounds:
// 1) each shard reports its max, 2) each shard gets the
// tallest bar to its left and right, and fills locally.
class ShardedWater {
    static long trap(List<int[]> shards) {
        int k = shards.size();
        int[] wallLeft = new int[k], wallRight = new int[k];
        for (int s = 1; s < k; s++) {
            wallLeft[s] = Math.max(wallLeft[s - 1],
                    max(shards.get(s - 1)));
        }
        for (int s = k - 2; s >= 0; s--) {
            wallRight[s] = Math.max(wallRight[s + 1],
                    max(shards.get(s + 1)));
        }
        long water = 0;
        for (int s = 0; s < k; s++) {   // in parallel
            water += fill(shards.get(s),
                    wallLeft[s], wallRight[s]);
        }
        return water;
    }

    // two pointers, seeded with the walls outside
    static long fill(int[] h, int maxLeft, int maxRight) {
        int left = 0, right = h.length - 1;
        long water = 0;
        while (left <= right) {
            maxLeft = Math.max(maxLeft, h[left]);
            maxRight = Math.max(maxRight, h[right]);
            if (maxLeft < maxRight) {
                water += maxLeft - h[left++];
            } else {
                water += maxRight - h[right--];
            }
        }
        return water;
    }

    static int max(int[] h) {
        int m = 0;
        for (int x : h) {
            m = Math.max(m, x);
        }
        return m;
    }
}
