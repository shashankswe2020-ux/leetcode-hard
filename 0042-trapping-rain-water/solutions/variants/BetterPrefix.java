import java.util.Arrays;

// The prefix version with Arrays.parallelPrefix (Java 8).
class BetterPrefix {
    public int trap(int[] bars) {
        int n = bars.length;
        int[] left = bars.clone();
        Arrays.parallelPrefix(left, Math::max);

        int[] back = new int[n];      // bars, reversed
        for (int i = 0; i < n; i++) {
            back[i] = bars[n - 1 - i];
        }
        Arrays.parallelPrefix(back, Math::max);

        int water = 0;
        for (int i = 0; i < n; i++) {
            int level = Math.min(left[i], back[n - 1 - i]);
            water += level - bars[i];
        }
        return water;
    }
}
