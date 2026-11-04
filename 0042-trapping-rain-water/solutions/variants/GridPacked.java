// One long per cell: height in the high 32 bits, cell id
// (row * cols + col) in the low bits. A hand-rolled
// min-heap of longs: no objects, no boxing.
class GridPacked {
    private long[] heap;
    private int size;

    public int trapRainWater(int[][] map) {
        int rows = map.length, cols = map[0].length;
        boolean[] seen = new boolean[rows * cols];
        heap = new long[rows * cols];
        size = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == 0 || c == 0
                        || r == rows - 1 || c == cols - 1) {
                    int id = r * cols + c;
                    push((long) map[r][c] << 32 | id);
                    seen[id] = true;
                }
            }
        }
        int water = 0;
        int[] dr = {1, -1, 0, 0}, dc = {0, 0, 1, -1};
        while (size > 0) {
            long low = pop();
            int h = (int) (low >>> 32), id = (int) low;
            for (int k = 0; k < 4; k++) {
                int r = id / cols + dr[k];
                int c = id % cols + dc[k];
                if (r < 0 || c < 0 || r >= rows || c >= cols
                        || seen[r * cols + c]) {
                    continue;
                }
                int next = r * cols + c;
                seen[next] = true;
                water += Math.max(0, h - map[r][c]);
                int level = Math.max(h, map[r][c]);
                push((long) level << 32 | next);
            }
        }
        return water;
    }

    private void push(long x) {
        int i = size++;
        while (i > 0 && heap[(i - 1) / 2] > x) {
            heap[i] = heap[(i - 1) / 2];
            i = (i - 1) / 2;
        }
        heap[i] = x;
    }

    private long pop() {
        long min = heap[0], x = heap[--size];
        int i = 0;
        while (2 * i + 1 < size) {
            int c = 2 * i + 1;
            if (c + 1 < size && heap[c + 1] < heap[c]) {
                c++;
            }
            if (heap[c] >= x) {
                break;
            }
            heap[i] = heap[c];
            i = c;
        }
        heap[i] = x;
        return min;
    }
}
