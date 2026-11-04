import java.util.Comparator;
import java.util.PriorityQueue;

class Solution {
    private static final int[][] DIRS =
            {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public int trapRainWater(int[][] map) {
        int rows = map.length, cols = map[0].length;
        boolean[][] seen = new boolean[rows][cols];
        PriorityQueue<int[]> wall =
                new PriorityQueue<>(
                        Comparator.comparingInt(a -> a[0]));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == 0 || c == 0
                        || r == rows - 1 || c == cols - 1) {
                    wall.add(new int[]{map[r][c], r, c});
                    seen[r][c] = true;
                }
            }
        }

        int water = 0;
        while (!wall.isEmpty()) {
            int[] low = wall.poll();
            for (int[] d : DIRS) {
                int r = low[1] + d[0], c = low[2] + d[1];
                if (r < 0 || c < 0 || r >= rows || c >= cols
                        || seen[r][c]) {
                    continue;
                }
                seen[r][c] = true;
                water += Math.max(0, low[0] - map[r][c]);
                wall.add(new int[]{
                        Math.max(low[0], map[r][c]), r, c});
            }
        }
        return water;
    }
}
