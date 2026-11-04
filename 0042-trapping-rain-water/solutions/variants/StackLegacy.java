import java.util.Stack;

// java.util.Stack: synchronized, extends Vector. Avoid.
class StackLegacy {
    public int trap(int[] bars) {
        Stack<Integer> stack = new Stack<>();
        int water = 0;
        for (int i = 0; i < bars.length; i++) {
            while (!stack.isEmpty()
                    && bars[stack.peek()] < bars[i]) {
                int floor = bars[stack.pop()];
                if (stack.isEmpty()) {
                    break;
                }
                int wall = stack.peek();
                int depth = Math.min(bars[wall], bars[i])
                        - floor;
                water += (i - wall - 1) * depth;
            }
            stack.push(i);
        }
        return water;
    }
}
