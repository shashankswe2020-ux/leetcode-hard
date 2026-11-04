class Solution {
    public int trap(int[] bars) {
        int[] stack = new int[bars.length];
        int top = -1;
        int water = 0;

        for (int i = 0; i < bars.length; i++) {
            while (top >= 0 && bars[stack[top]] < bars[i]) {
                int floor = bars[stack[top--]];
                if (top < 0) {
                    break;
                }
                int wall = stack[top];
                int width = i - wall - 1;
                int depth = Math.min(bars[wall], bars[i])
                        - floor;
                water += width * depth;
            }
            stack[++top] = i;
        }
        return water;
    }
}
