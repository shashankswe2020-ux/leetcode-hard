import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.IntStream;

// Java 24+: the running max as a stream (Gatherers.scan).
class BetterScan {
    public int trap(int[] bars) {
        int n = bars.length;
        List<Integer> left = IntStream.of(bars).boxed()
                .gather(Gatherers.scan(() -> 0, Math::max))
                .toList();
        List<Integer> right = IntStream.range(0, n)
                .mapToObj(i -> bars[n - 1 - i])
                .gather(Gatherers.scan(() -> 0, Math::max))
                .toList()
                .reversed();

        int water = 0;
        for (int i = 0; i < n; i++) {
            int level = Math.min(left.get(i), right.get(i));
            water += level - bars[i];
        }
        return water;
    }
}
