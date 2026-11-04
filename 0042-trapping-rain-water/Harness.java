import java.util.*;
import java.util.function.ToIntFunction;

/** Day 002 verification: examples, edge cases, differential test, limits, 2D, shards, homework, timing. */
public class Harness {
    record Impl(String name, ToIntFunction<int[]> f) {}

    static final List<Impl> IMPLS = List.of(
        new Impl("brute",   b -> new Brute().trap(b)),
        new Impl("better",  b -> new Better().trap(b)),
        new Impl("optimal", b -> new Optimal().trap(b)),
        new Impl("stack",   b -> new StackInts().trap(b)),
        new Impl("modern",  b -> new Modern().trap(b)),
        new Impl("deque",   b -> new StackDeque().trap(b)),
        new Impl("legacy",  b -> new StackLegacy().trap(b)),
        new Impl("prefix",  b -> new BetterPrefix().trap(b)),
        new Impl("scan",    b -> new BetterScan().trap(b)),
        new Impl("sharded", b -> (int) ShardedWater.trap(split(b, new Random(b.length), 1 + b.length % 7))));

    /** Independent reference: fill layer by layer. Level y holds water at i if a bar >= y stands on both sides. */
    static long layers(int[] b) {
        int top = 0; for (int x : b) top = Math.max(top, x);
        long water = 0;
        for (int y = 1; y <= top; y++) {
            int first = -1, last = -1;
            for (int i = 0; i < b.length; i++) if (b[i] >= y) { if (first < 0) first = i; last = i; }
            for (int i = first + 1; i < last; i++) if (b[i] < y) water++;
        }
        return water;
    }

    /** Independent reference for big heights: level = min(max left, max right), in long math. */
    static long ref(int[] b) {
        long water = 0;
        for (int i = 0; i < b.length; i++) {
            long l = 0, r = 0;
            for (int j = 0; j <= i; j++) l = Math.max(l, b[j]);
            for (int j = i; j < b.length; j++) r = Math.max(r, b[j]);
            water += Math.min(l, r) - b[i];
        }
        return water;
    }

    /** Linear reference for very large inputs (prefix and suffix max in long math). */
    static long ref2(int[] b) { int n = b.length; long[] L = new long[n], R = new long[n]; long w = 0;
        for (int i = 0; i < n; i++) L[i] = Math.max(i > 0 ? L[i - 1] : 0, b[i]);
        for (int i = n - 1; i >= 0; i--) R[i] = Math.max(i < n - 1 ? R[i + 1] : 0, b[i]);
        for (int i = 0; i < n; i++) w += Math.min(L[i], R[i]) - b[i]; return w; }

    static List<int[]> split(int[] b, Random r, int k) {
        k = Math.max(1, Math.min(k, b.length));
        TreeSet<Integer> cuts = new TreeSet<>();
        while (cuts.size() < k - 1) cuts.add(1 + r.nextInt(b.length - 1));
        List<int[]> out = new ArrayList<>(); int from = 0;
        for (int c : cuts) { out.add(Arrays.copyOfRange(b, from, c)); from = c; }
        out.add(Arrays.copyOfRange(b, from, b.length));
        return out;
    }

    /** Shards behind a simulated network: every call sleeps for latencyMs. */
    static List<FanOut.Shard> remote(List<int[]> parts, int latencyMs) {
        List<FanOut.Shard> out = new ArrayList<>();
        for (int[] p : parts) out.add(new FanOut.Shard() {
            void wait_() { if (latencyMs > 0) try { Thread.sleep(latencyMs); } catch (InterruptedException e) { throw new RuntimeException(e); } }
            public int max() { wait_(); return ShardedWater.max(p); }
            public long fill(int l, int r) { wait_(); return ShardedWater.fill(p, l, r); } });
        return out;
    }

    static int fails = 0;
    static void check(String label, int[] b) {
        long want = b.length <= 400 && Arrays.stream(b).max().orElse(0) <= 60 ? layers(b) : ref(b);
        if (b.length <= 400 && want != ref(b)) { fails++; System.out.println("REF MISMATCH " + label); }
        for (Impl im : IMPLS) {
            long got;
            try { got = im.f().applyAsInt(b.clone()); } catch (RuntimeException e) { got = Long.MIN_VALUE; }
            if (got != want) {
                fails++;
                System.out.printf("FAIL %-8s %s bars=%s want=%d got=%d%n", im.name(), label,
                    b.length > 30 ? "len " + b.length : Arrays.toString(b), want, got);
            }
        }
    }

    static int[] rand(Random r, int n, int hi) { int[] b = new int[n]; for (int i = 0; i < n; i++) b[i] = r.nextInt(hi + 1); return b; }

    // ---------- 2D reference: level = min over paths to the border of the max bar on the path (Bellman-Ford style)
    static long ref2d(int[][] m) {
        int R = m.length, C = m[0].length; long[][] lv = new long[R][C];
        for (int r = 0; r < R; r++) for (int c = 0; c < C; c++)
            lv[r][c] = (r == 0 || c == 0 || r == R - 1 || c == C - 1) ? m[r][c] : Long.MAX_VALUE;
        boolean changed = true; int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
        while (changed) {
            changed = false;
            for (int r = 1; r < R - 1; r++) for (int c = 1; c < C - 1; c++) {
                long best = lv[r][c];
                for (int[] q : d) best = Math.min(best, Math.max(m[r][c], lv[r + q[0]][c + q[1]]));
                if (best < lv[r][c]) { lv[r][c] = best; changed = true; }
            }
        }
        long w = 0; for (int r = 0; r < R; r++) for (int c = 0; c < C; c++) w += lv[r][c] - m[r][c];
        return w;
    }

    static int areaBrute(int[] b) { int best = 0;
        for (int i = 0; i < b.length; i++) for (int j = i + 1; j < b.length; j++) best = Math.max(best, Math.min(b[i], b[j]) * (j - i));
        return best; }

    public static void main(String[] args) {
        boolean timing = !Arrays.asList(args).contains("--no-timing");
        System.out.println("JVM " + Runtime.version() + ", " + Runtime.getRuntime().availableProcessors() + " cores");
        long seed = args.length > 0 && !args[0].startsWith("-") ? Long.parseLong(args[0]) : 20261103L;

        // 1. The episode example and LeetCode-shaped examples
        int[] ex = {2, 0, 3, 1, 0, 4, 1, 2, 0, 3};
        check("episode", ex);
        System.out.println("episode example " + Arrays.toString(ex) + " -> " + new Optimal().trap(ex.clone()));
        check("v-shape", new int[]{3, 0, 3});
        check("staircase-pool", new int[]{4, 2, 0, 3, 2, 5});

        // 2. Edge cases
        check("one", new int[]{5});
        check("two", new int[]{5, 1});
        check("all-zero", new int[]{0, 0, 0, 0});
        check("all-equal", new int[]{3, 3, 3, 3});
        check("increasing", new int[]{0, 1, 2, 3, 4, 5});
        check("decreasing", new int[]{5, 4, 3, 2, 1, 0});
        check("peak", new int[]{1, 3, 5, 3, 1});
        check("valley", new int[]{5, 3, 1, 3, 5});
        check("plateau-walls", new int[]{4, 4, 0, 0, 4, 4});
        check("equal-walls-tie", new int[]{3, 1, 2, 1, 3});
        check("two-pools", new int[]{3, 0, 3, 0, 3});
        int N = 20_000, H = 100_000;
        int[] maxCase = new int[N]; maxCase[0] = H; maxCase[N - 1] = H;
        check("max-water", maxCase);
        int[] saw = new int[N]; for (int i = 0; i < N; i += 2) saw[i] = H;
        check("saw", saw);
        System.out.println("examples + edge cases: " + (fails == 0 ? "PASS" : "FAILS=" + fails));
        System.out.println("max water at the limits: " + ref(maxCase) + " (int max " + Integer.MAX_VALUE + ")");

        // 3. Differential test
        Random r = new Random(seed); int cases = 0;
        for (; cases < 10_000; cases++) {
            int n = 1 + r.nextInt(r.nextBoolean() ? 12 : 60);
            check("rand#" + cases, rand(r, n, r.nextBoolean() ? 5 : 60));
        }
        for (int k = 0; k < 200; k++, cases++) check("big#" + k, rand(r, 1 + r.nextInt(N), r.nextBoolean() ? 10 : H));
        System.out.println("differential: " + cases + " random cases, seed " + seed + ": " + (fails == 0 ? "ALL AGREE" : "FAILS=" + fails));

        // 4. Beyond constraints: 30,000 bars, two walls of 100,000
        int[] over = new int[30_000]; over[0] = H; over[over.length - 1] = H;
        String modernOver;
        try { modernOver = String.valueOf(new Modern().trap(over)); } catch (ArithmeticException e) { modernOver = "ArithmeticException: " + e.getMessage(); }
        System.out.println("beyond limits (30,000 bars): want " + ref(over) + " optimal " + new Optimal().trap(over) + " modern " + modernOver);

        // 5. Shards: many random splits of random inputs
        int shardFails = 0;
        for (int t = 0; t < 2000; t++) {
            int[] b = rand(r, 1 + r.nextInt(300), r.nextBoolean() ? 8 : H);
            List<int[]> sh = split(b, r, 1 + r.nextInt(Math.min(40, b.length)));
            if (ShardedWater.trap(sh) != ref(b)) shardFails++;
        }
        fails += shardFails;
        System.out.println("sharded: 2000 random inputs, 1..40 shards: " + (shardFails == 0 ? "ALL AGREE" : "FAILS=" + shardFails));

        // 6. 2D (LeetCode 407) against the minimax-path reference
        int gridFails = 0;
        for (int t = 0; t < 3000; t++) {
            int R = 1 + r.nextInt(8), C = 1 + r.nextInt(8), hi = r.nextBoolean() ? 6 : 20_000;
            int[][] m = new int[R][C]; for (int[] row : m) for (int c = 0; c < C; c++) row[c] = r.nextInt(hi + 1);
            long want2 = ref2d(m);
            if (new Grid().trapRainWater(m) != want2) gridFails++;
            if (new GridModern().trapRainWater(m) != want2) gridFails++;
            if (new GridRecord().trapRainWater(m) != want2) gridFails++;
            if (new GridPacked().trapRainWater(m) != want2) gridFails++;
        }
        int[][] g = {{3, 3, 3, 3, 3}, {3, 1, 2, 1, 3}, {3, 2, 0, 2, 3}, {3, 1, 2, 1, 3}, {3, 3, 3, 3, 3}};
        fails += gridFails;
        System.out.println("2D: 3000 random maps up to 8x8: " + (gridFails == 0 ? "ALL AGREE" : "FAILS=" + gridFails)
            + "; bowl example -> " + new Grid().trapRainWater(g));
        { int[][] big = new int[200][200]; Random rr = new Random(7);
          for (int[] row : big) for (int c = 0; c < 200; c++) row[c] = rr.nextInt(20_001);
          long want = ref2d(big); long[] got = {new Grid().trapRainWater(big), new GridModern().trapRainWater(big),
              new GridRecord().trapRainWater(big), new GridPacked().trapRainWater(big)};
          boolean ok = true; for (long g2 : got) ok &= g2 == want; if (!ok) fails++;
          System.out.println("2D at 200x200: all four heaps " + Arrays.toString(got) + " vs reference " + want + (ok ? " AGREE" : " FAIL")); }

        // 7. Homework: LeetCode 11 against O(n^2)
        int hwFails = 0;
        for (int t = 0; t < 5000; t++) { int[] b = rand(r, 2 + r.nextInt(40), r.nextBoolean() ? 5 : 1000);
            if (new Container().maxArea(b) != areaBrute(b)) hwFails++; }
        fails += hwFails;
        System.out.println("homework container: 5000 random cases: " + (hwFails == 0 ? "ALL AGREE" : "FAILS=" + hwFails));

        // 8. The trap version: moving the pointer by comparing the running maxes vs the bars (both correct), and a real bug
        { int[] b = ex; int n = b.length, w = 0;                             // bug: max of the OTHER bars, excluding i
          for (int i = 0; i < n; i++) { int l = 0, rr = 0; for (int j = 0; j < i; j++) l = Math.max(l, b[j]);
            for (int j = i + 1; j < n; j++) rr = Math.max(rr, b[j]); w += Math.min(l, rr) - b[i]; }
          System.out.println("bug 'walls exclude the bar itself' on the episode example: " + w + " instead of " + ref(b)); }
        { int[] b = ex; int left = 0, right = b.length - 1, maxL = 0, maxR = 0, w = 0;   // bug: fill from the side with the TALLER bar
          while (left < right) { if (b[left] > b[right]) { maxL = Math.max(maxL, b[left]); w += maxL - b[left++]; }
            else { maxR = Math.max(maxR, b[right]); w += maxR - b[right--]; } }
          System.out.println("bug 'move the taller side' on the episode example: " + w + " instead of " + ref(b)); }
        { int[] b = ex; int w = 0;                                            // bug: only the left max
          int maxL = 0; for (int x : b) { maxL = Math.max(maxL, x); w += maxL - x; }
          System.out.println("bug 'left max only' on the episode example: " + w + " instead of " + ref(b)); }

        // 8b. Sharding needs the max-comparing loop: the bar-comparing loop (c5) breaks once seeded with an outside wall
        { int[] b = {1, 5}; int left = 0, right = 1, maxL = 10, maxR = 0, w = 0;
          while (left < right) { if (b[left] < b[right]) { maxL = Math.max(maxL, b[left]); w += maxL - b[left++]; }
            else { maxR = Math.max(maxR, b[right]); w += maxR - b[right--]; } }
          System.out.println("seeded shard [1,5], wall 10 on the left: bar-comparing loop " + w + ", max-comparing fill "
            + ShardedWater.fill(b, 10, 0) + ", true " + (ref(new int[]{10, 1, 5}))); }
        // 8c. Stream (StreamWater: pops equal heights, long total): after every bar the total equals the prefix answer
        { int streamFails = 0;
          for (int t = 0; t < 3000; t++) { int[] b = rand(r, 1 + r.nextInt(80), r.nextBoolean() ? 6 : 1000);
            StreamWater sw = new StreamWater(1000); long prev = 0;
            for (int i = 0; i < b.length; i++) { long w = sw.add(b[i]);
              if (w != ref(Arrays.copyOf(b, i + 1)) || w < prev) streamFails++; prev = w; } }
          fails += streamFails;
          System.out.println("stream: running total equals the prefix answer after every bar (3000 streams): " + (streamFails == 0 ? "ALL AGREE" : "FAILS=" + streamFails)); }
        // 8d. Memory bound: the strictly falling stack holds at most one bar per distinct height (heights 0..H -> H+1)
        { StreamWater sw = new StreamWater(100_000); int peak = 0;
          for (int i = 0; i < 1_000_000; i++) { sw.add(100_000 - i % 100_001); peak = Math.max(peak, sw.depth()); }
          Random rs = new Random(5); for (int i = 0; i < 1_000_000; i++) { sw.add(rs.nextInt(100_001)); peak = Math.max(peak, sw.depth()); }
          if (peak > 100_001) fails++;
          System.out.println("stream of 2*10^6 bars (falling staircases, then random), heights 0..10^5: stack peak " + peak + " (bound 100,001)"); }

        // 8f. c9 counterexample: walled in by 9s on all four straight lines, yet it drains around a corner
        { int[][] corner = {{9,9,9,0,9},{9,9,9,0,9},{9,9,0,0,9},{9,9,9,9,9},{9,9,9,9,9}};
          int straight = Math.min(Math.min(Math.max(corner[2][0], corner[2][1]), Math.max(corner[2][3], corner[2][4])),
                                  Math.min(Math.max(corner[0][2], corner[1][2]), Math.max(corner[3][2], corner[4][2])));
          long truth = ref2d(corner), heap = new Grid().trapRainWater(corner);
          if (truth != 0 || heap != 0) fails++;
          System.out.println("2D corner map: straight-line level " + straight + ", water by reference " + truth + ", by the heap " + heap); }
        // 8g. c12 widths: two pointers with widths == the answer for the bars expanded into unit bars
        { int wFails = 0;
          for (int t = 0; t < 3000; t++) { int n = 1 + r.nextInt(30); int[] b = rand(r, n, 20), wd = new int[n];
            int total = 0; for (int i = 0; i < n; i++) { wd[i] = 1 + r.nextInt(4); total += wd[i]; }
            int[] unit = new int[total]; for (int i = 0, k = 0; i < n; i++) for (int q = 0; q < wd[i]; q++) unit[k++] = b[i];
            int left = 0, right = n - 1, maxL = 0, maxR = 0; long w = 0;
            while (left < right) { if (b[left] < b[right]) { maxL = Math.max(maxL, b[left]); w += (long) (maxL - b[left]) * wd[left]; left++; }
              else { maxR = Math.max(maxR, b[right]); w += (long) (maxR - b[right]) * wd[right]; right--; } }
            if (w != ref(unit)) wFails++; }
          fails += wFails;
          System.out.println("widths: two pointers with water times width vs expanded unit bars (3000 inputs): " + (wFails == 0 ? "ALL AGREE" : "FAILS=" + wFails)); }

        // 8h. FanOut (c10/c11): the coordinator's two rounds over "remote" shards agree with ShardedWater
        { int foFails = 0;
          try {
            for (int t = 0; t < 300; t++) { int[] b = rand(r, 1 + r.nextInt(300), r.nextBoolean() ? 8 : H);
              List<int[]> sh = split(b, r, 1 + r.nextInt(Math.min(40, b.length)));
              if (FanOut.trap(remote(sh, 0)) != ref(b)) foFails++; }
          } catch (Exception e) { foFails++; }
          fails += foFails;
          System.out.println("fan-out coordinator on virtual threads: 300 random clusters: " + (foFails == 0 ? "ALL AGREE" : "FAILS=" + foFails)); }

        // 8e. Invariant two (c6): the tallest bar seen so far always sits under one of the two pointers
        { int bad = 0;
          for (int t = 0; t < 3000; t++) { int[] b = rand(r, 2 + r.nextInt(60), r.nextBoolean() ? 6 : 1000);
            int left = 0, right = b.length - 1, seen = Math.max(b[0], b[b.length - 1]);
            while (left < right) {
              if (Math.max(b[left], b[right]) != seen) bad++;
              if (b[left] < b[right]) left++; else right--;
              seen = Math.max(seen, Math.max(b[left], b[right])); } }
          fails += bad;
          System.out.println("invariant: tallest bar seen is under a pointer (3000 inputs): " + (bad == 0 ? "ALWAYS" : "BROKEN " + bad)); }

        // 9. Steps on the episode example and at the limit
        { int[] b = ex; int left = 0, right = b.length - 1, moves = 0;
          while (left < right) { if (b[left] < b[right]) left++; else right--; moves++; }
          System.out.println("optimal pointer moves on the example: " + moves); }
        { int[] dec = new int[N]; for (int i = 0; i < N; i++) dec[i] = N - i;
          int top = -1, peak = 0; int[] st = new int[N];
          for (int i = 0; i < N; i++) { while (top >= 0 && dec[st[top]] < dec[i]) top--; st[++top] = i; peak = Math.max(peak, top + 1); }
          System.out.println("stack peak depth on a decreasing staircase of " + N + ": " + peak); }

        if (!timing) System.exit(fails == 0 ? 0 : 1);

        // 10. Timing (median of 15 runs after warm-up), ns per call, random bars: n = 20,000 (heights to 10^5), n = 10^6 (heights to 10^3)
        for (int n : new int[]{20_000, 1_000_000}) {
            int[] b = rand(r, n, n > 20_000 ? 1_000 : H);   // keep the total inside int at 10^6
            StringBuilder line = new StringBuilder("n=" + n + ":");
            for (Impl im : IMPLS) {
                if (im.name().equals("brute") && n > 20_000) { line.append(" brute=skipped"); continue; }
                if (im.name().equals("sharded")) continue;
                int reps = im.name().equals("brute") ? 3 : Math.max(5, 20_000_000 / n);
                long[] runs = new long[15]; long sink = 0;
                for (int w = 0; w < 3; w++) for (int k = 0; k < reps; k++) sink += im.f().applyAsInt(b);
                for (int t = 0; t < runs.length; t++) { long t0 = System.nanoTime();
                    for (int k = 0; k < reps; k++) sink += im.f().applyAsInt(b);
                    runs[t] = (System.nanoTime() - t0) / reps; }
                Arrays.sort(runs);
                line.append(String.format(" %s=%,dns", im.name(), runs[runs.length / 2]));
                if (sink == 42) System.out.print("");
            }
            System.out.println(line);
        }
        // 12. Fan-out: 1,000 shards, 10 ms per call, two rounds. Fixed pool of 64 platform threads, one platform
        //     thread per call (1,000 OS threads), one virtual thread per call (FanOut.trap(shards), the shipped line)
        { int[] b = rand(new Random(13), 1_000_000, H); List<FanOut.Shard> sh = remote(split(b, new Random(14), 1000), 10);
          long want = ref2(b); StringBuilder line = new StringBuilder("fan-out 1,000 shards x 10 ms:");
          String[] modes = {"fixed pool of 64 platform threads", "platform thread per call", "virtual thread per call"};
          for (int mode = 0; mode < 3; mode++) { long[] runs = new long[5];
            for (int t = 0; t < runs.length; t++) {
              try {
                long t0 = System.nanoTime(), got;
                if (mode == 2) got = FanOut.trap(sh);
                else try (var pool = mode == 0 ? java.util.concurrent.Executors.newFixedThreadPool(64)
                        : java.util.concurrent.Executors.newThreadPerTaskExecutor(java.util.concurrent.Executors.defaultThreadFactory())) {
                  got = FanOut.trap(sh, pool); }
                runs[t] = System.nanoTime() - t0;
                if (got != want) { fails++; System.out.println("FANOUT FAIL"); } }
              catch (Exception e) { fails++; } }
            Arrays.sort(runs); line.append(String.format(" %s=%,dms;", modes[mode], runs[2] / 1_000_000)); }
          System.out.println(line); }
        // 13. Tip one, paired: optimal (int total) vs modern (long total), alternating 31 trials to cancel drift
        for (int n : new int[]{20_000, 1_000_000}) {
          int[] b = rand(new Random(1), n, n > 20_000 ? 1_000 : H); Optimal o = new Optimal(); Modern m = new Modern();
          long sink = 0; int reps = 20_000_000 / n; long[] to = new long[31], tm = new long[31];
          for (int w = 0; w < 200; w++) { sink += o.trap(b); sink += m.trap(b); }
          for (int t = 0; t < 31; t++) {
            long t0 = System.nanoTime(); for (int k = 0; k < reps; k++) sink += o.trap(b); to[t] = (System.nanoTime() - t0) / reps;
            t0 = System.nanoTime(); for (int k = 0; k < reps; k++) sink += m.trap(b); tm[t] = (System.nanoTime() - t0) / reps; }
          Arrays.sort(to); Arrays.sort(tm);
          System.out.printf("tip one paired, n=%d: int total %,dns, long total %,dns (%+.1f%%)%n", n, to[15], tm[15], 100.0 * (tm[15] - to[15]) / to[15]);
          if (sink == 1) System.out.print(""); }
        // 11. 2D timing at 200 x 200 (heights to 2*10^4): int[] entries vs record vs packed long
        { int[][] big = new int[200][200]; Random rr = new Random(11);
          for (int[] row : big) for (int c = 0; c < 200; c++) row[c] = rr.nextInt(20_001);
          String[] names = {"grid int[]", "grid comparingInt", "grid record", "grid packed long"};
          java.util.List<ToIntFunction<int[][]>> fs = List.of(m -> new Grid().trapRainWater(m), m -> new GridModern().trapRainWater(m),
              m -> new GridRecord().trapRainWater(m), m -> new GridPacked().trapRainWater(m));
          StringBuilder line = new StringBuilder("2D 200x200:");
          for (int k = 0; k < fs.size(); k++) { long[] runs = new long[15]; long sink = 0;
            for (int w = 0; w < 20; w++) sink += fs.get(k).applyAsInt(big);
            for (int t = 0; t < runs.length; t++) { long t0 = System.nanoTime(); for (int q = 0; q < 10; q++) sink += fs.get(k).applyAsInt(big); runs[t] = (System.nanoTime() - t0) / 10; }
            Arrays.sort(runs); line.append(String.format(" %s=%,dns", names[k], runs[7])); if (sink == 42) System.out.print(""); }
          System.out.println(line); }
        System.exit(fails == 0 ? 0 : 1);
    }
}
