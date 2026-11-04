// The stack, made for a stream: bars arrive one by one.
// Pops equal heights too, so the stack strictly falls and
// holds at most one entry per height: memory O(max height).
class StreamWater {
    private final int[] stackHeight;
    private final long[] stackIndex;
    private int top = -1;
    private long count = 0;
    private long water = 0;

    StreamWater(int maxHeight) {
        stackHeight = new int[maxHeight + 2];
        stackIndex = new long[maxHeight + 2];
    }

    long add(int bar) {
        if (bar < 0 || bar >= stackHeight.length - 1) {
            throw new IllegalArgumentException("height");
        }
        long i = count++;
        while (top >= 0 && stackHeight[top] <= bar) {
            int floor = stackHeight[top--];
            if (top < 0) {
                break;
            }
            int wall = stackHeight[top];
            long width = i - stackIndex[top] - 1;
            int depth = Math.min(wall, bar) - floor;
            water += width * depth;
        }
        top++;
        stackHeight[top] = bar;
        stackIndex[top] = i;
        return water;
    }

    int depth() {
        return top + 1;
    }
}
