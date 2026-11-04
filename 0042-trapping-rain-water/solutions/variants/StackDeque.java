import java.util.ArrayDeque;
import java.util.Deque;

// The usual way to write the stack: boxed Integers.
class StackDeque {
    public int trap(int[] bars) {
        Deque<Integer> stack = new ArrayDeque<>();
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
