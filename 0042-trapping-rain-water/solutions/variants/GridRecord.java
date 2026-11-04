import java.util.Comparator;
import java.util.PriorityQueue;

// Java 16+: a record for the heap entries.
class GridRecord {
    record Cell(int height, int row, int col) {}

    private static final int[][] DIRS =
            {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public int trapRainWater(int[][] map) {
        int rows = map.length, cols = map[0].length;
        boolean[][] seen = new boolean[rows][cols];
        PriorityQueue<Cell> wall = new PriorityQueue<>(
                Comparator.comparingInt(Cell::height));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == 0 || c == 0
                        || r == rows - 1 || c == cols - 1) {
                    wall.add(new Cell(map[r][c], r, c));
                    seen[r][c] = true;
                }
            }
        }
        int water = 0;
        while (!wall.isEmpty()) {
            Cell low = wall.poll();
            for (int[] d : DIRS) {
                int r = low.row() + d[0];
                int c = low.col() + d[1];
                if (r < 0 || c < 0 || r >= rows || c >= cols
                        || seen[r][c]) {
                    continue;
                }
                seen[r][c] = true;
                int depth = low.height() - map[r][c];
                water += Math.max(0, depth);
                int level =
                        Math.max(low.height(), map[r][c]);
                wall.add(new Cell(level, r, c));
            }
        }
        return water;
    }
}
