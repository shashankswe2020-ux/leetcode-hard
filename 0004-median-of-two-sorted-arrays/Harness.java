import java.util.*;
import java.util.function.BiFunction;

/** Day 001 verification: examples, edge cases, differential test, limit timing. */
public class Harness {
    record Impl(String name, BiFunction<int[], int[], Double> f) {}

    static final List<Impl> IMPLS = List.of(
        new Impl("brute",   (a, b) -> new Brute().findMedianSortedArrays(a, b)),
        new Impl("better",  (a, b) -> new Better().findMedianSortedArrays(a, b)),
        new Impl("optimal", (a, b) -> new Optimal().findMedianSortedArrays(a, b)),
        new Impl("modern",  (a, b) -> new Modern().findMedianSortedArrays(a, b)),
        new Impl("kth",     (a, b) -> new Kth().findMedianSortedArrays(a, b)),
        new Impl("value",   (a, b) -> new Value().findMedianSortedArrays(a, b)),
        new Impl("sharded", (a, b) -> {
            java.util.function.LongToIntFunction sa = v -> upper(a, v), sb = v -> upper(b, v);
            return ShardedMedian.median(List.of(sa, sb), (long) a.length + b.length); }));

    static int upper(int[] x, long v) { int lo = 0, hi = x.length;
        while (lo < hi) { int mid = (lo + hi) >>> 1; if (x[mid] <= v) lo = mid + 1; else hi = mid; } return lo; }

    /** Independent reference: merge in long arithmetic. */
    static double ref(int[] a, int[] b) {
        long[] all = new long[a.length + b.length];
        for (int i = 0; i < a.length; i++) all[i] = a[i];
        for (int i = 0; i < b.length; i++) all[a.length + i] = b[i];
        Arrays.sort(all);
        int n = all.length, mid = n / 2;
        return n % 2 == 1 ? all[mid] : (all[mid - 1] + all[mid]) / 2.0;
    }

    static int fails = 0;
    static void check(String label, int[] a, int[] b, Set<String> skip) {
        double want = ref(a, b);
        for (Impl im : IMPLS) {
            if (skip.contains(im.name())) continue;
            double got;
            try { got = im.f().apply(a.clone(), b.clone()); }
            catch (RuntimeException e) { got = Double.NaN; }
            if (Double.compare(got, want) != 0) {
                fails++;
                System.out.printf("FAIL %-8s %s a=%s b=%s want=%s got=%s%n", im.name(), label,
                    Arrays.toString(a), Arrays.toString(b), want, got);
            }
        }
    }
    static void check(String label, int[] a, int[] b) { check(label, a, b, Set.of()); }

    static int[] sortedRandom(Random r, int len, int lo, int hi) {
        int[] x = new int[len];
        for (int i = 0; i < len; i++) x[i] = lo + r.nextInt(hi - lo + 1);
        Arrays.sort(x);
        return x;
    }

    public static void main(String[] args) {
        // 1. Examples used in the episode
        check("ex1", new int[]{1, 3}, new int[]{2});
        check("ex2", new int[]{1, 2}, new int[]{3, 4});
        check("ep-odd", new int[]{1, 4, 7, 10, 13}, new int[]{2, 3, 6, 15});
        check("ep-even", new int[]{1, 4, 7, 10}, new int[]{2, 3, 6, 15});

        // 2. Edge cases
        int M = 1_000_000;
        check("a-empty", new int[]{}, new int[]{5});
        check("b-empty", new int[]{1, 2, 3, 4}, new int[]{});
        check("one-each", new int[]{1}, new int[]{2});
        check("all-equal", new int[]{7, 7, 7}, new int[]{7, 7});
        check("a-all-smaller", new int[]{1, 2, 3}, new int[]{10, 11, 12, 13});
        check("a-all-larger", new int[]{10, 11, 12, 13}, new int[]{1, 2, 3});
        check("negatives", new int[]{-5, -3, -1}, new int[]{-4, -2});
        check("constraint-extremes", new int[]{-M, -M}, new int[]{M, M});
        check("interleaved", new int[]{1, 3, 5, 7}, new int[]{2, 4, 6, 8});
        check("single-a-long-b", new int[]{100}, new int[]{1, 2, 3, 4, 5, 6});
        System.out.println("examples + edge cases: " + (fails == 0 ? "PASS" : "FAILS=" + fails));

        // 3. Differential test
        long seed = args.length > 0 && !args[0].startsWith("-") ? Long.parseLong(args[0]) : 20261102L;
        Random r = new Random(seed);
        int cases = 0;
        for (; cases < 10_000; cases++) {
            int m = r.nextInt(13), n = r.nextInt(13);
            if (m + n == 0) n = 1;
            boolean narrow = r.nextBoolean();  // narrow range forces duplicates
            int lo = narrow ? -10 : -M, hi = narrow ? 10 : M;
            check("rand#" + cases, sortedRandom(r, m, lo, hi), sortedRandom(r, n, lo, hi));
        }
        for (int k = 0; k < 200; k++, cases++) {  // max-constraint sizes
            int m = r.nextInt(1001), n = r.nextInt(1001);
            if (m + n == 0) n = 1;
            check("big#" + k, sortedRandom(r, m, -M, M), sortedRandom(r, n, -M, M));
        }
        System.out.println("differential: " + cases + " random cases, seed " + seed + ": "
            + (fails == 0 ? "ALL AGREE" : "FAILS=" + fails));

        // 4. Beyond constraints: full int range shows the overflow the modern pass fixes
        int[] hugeA = {Integer.MAX_VALUE - 1}, hugeB = {Integer.MAX_VALUE};
        System.out.printf("full-int even median: want %.1f optimal %.1f modern %.1f%n",
            ref(hugeA, hugeB), new Optimal().findMedianSortedArrays(hugeA, hugeB),
            new Modern().findMedianSortedArrays(hugeA, hugeB));

        // 4b. Many shards: 50 random tests with 2..40 shards
        int shardFails = 0, maxRounds = 0;
        for (int tcase = 0; tcase < 50; tcase++) {
            int ns = 2 + r.nextInt(39); List<int[]> data = new ArrayList<>();
            List<java.util.function.LongToIntFunction> fs = new ArrayList<>(); long tot = 0;
            for (int s = 0; s < ns; s++) { int[] x = sortedRandom(r, r.nextInt(200), -M, M); data.add(x); tot += x.length;
                fs.add(v -> upper(x, v)); }
            if (tot == 0) continue;
            long[] all = data.stream().flatMapToInt(Arrays::stream).asLongStream().sorted().toArray();
            int nn = all.length; double want = nn % 2 == 1 ? all[nn / 2] : (all[nn / 2 - 1] + all[nn / 2]) / 2.0;
            if (Double.compare(ShardedMedian.median(fs, tot), want) != 0) shardFails++;
        }
        System.out.println("sharded: 50 random clusters of 2..40 shards: " + (shardFails == 0 ? "ALL AGREE" : "FAILS=" + shardFails));
        // the bugs chapter: integer division, and >= in the checks
        System.out.println("bug 1, (2 + 3) / 2 on [1,2]+[3,4]: " + (2 + 3) / 2 + " instead of " + (2 + 3) / 2.0);
        { int[] a = {7, 7}, b = {7, 7, 7}; int m = 2, n = 3, half = (m + n + 1) / 2, lo = 0, hi = m; boolean found = false;
          while (lo <= hi) { int ca = (lo + hi) / 2, cb = half - ca;
            int al = ca == 0 ? Integer.MIN_VALUE : a[ca - 1], ar = ca == m ? Integer.MAX_VALUE : a[ca];
            int bl = cb == 0 ? Integer.MIN_VALUE : b[cb - 1], br = cb == n ? Integer.MAX_VALUE : b[cb];
            if (al >= br) hi = ca - 1; else if (bl >= ar) lo = ca + 1; else { found = true; break; } }
          System.out.println("bug 4, >= in the checks on five 7s: " + (found ? "found a cut" : "no cut found, would throw")); }
        // homework: k-th smallest with the cut, every k, against the merged order
        { int bad = 0, kcases = 0; Random hr = new Random(4);
          for (int tcase = 0; tcase < 20_000; tcase++) {
            int[] a = sortedRandom(hr, hr.nextInt(8), -20, 20), b = sortedRandom(hr, hr.nextInt(8), -20, 20);
            int[] c = new int[a.length + b.length];
            System.arraycopy(a, 0, c, 0, a.length); System.arraycopy(b, 0, c, a.length, b.length); Arrays.sort(c);
            for (int k = 1; k <= c.length; k++) { kcases++; if (KthSmallest.kth(a, b, k) != c[k - 1]) bad++; } }
          fails += bad;
          System.out.println("homework k-th via the cut: " + kcases + " (arrays, k) cases: " + (bad == 0 ? "ALL AGREE" : "FAILS=" + bad)); }
        // rounds of the value search over the full int range
        { long lo = Integer.MIN_VALUE, hi = Integer.MAX_VALUE; int rounds = 0;
          while (lo < hi) { long mid = Math.floorDiv(lo + hi, 2); rounds++; if (mid >= 0) hi = mid; else lo = mid + 1; }
          System.out.println("value search rounds over full int range: " + rounds); }
        // trap: midpoint with / instead of floorDiv on negatives never shrinks lo=-3, hi=-2 when the answer is -3
        { long lo = -3, hi = -2; int it = 0;
          while (lo < hi && it < 1000) { long mid = (lo + hi) / 2; it++; if (mid >= -3) hi = mid; else lo = mid + 1; }
          System.out.println("trap '/' midpoint on negatives: lo=" + lo + " hi=" + hi + " after " + it + " iterations (stuck if 1000)"); }
        // max probes of optimal at the constraint limit (instrumented copy of the loop)
        { int worst = 0;
          for (int tcase = 0; tcase < 20000; tcase++) {
            int[] a = sortedRandom(r, r.nextInt(1001), -M, M), b = sortedRandom(r, 1000, -M, M);
            if (a.length > b.length) { int[] t2 = a; a = b; b = t2; }
            int m = a.length, n = b.length, half = (m + n + 1) / 2, lo = 0, hi = m, probes = 0;
            while (lo <= hi) { int ca = (lo + hi) / 2, cb = half - ca; probes++;
              int al = ca == 0 ? Integer.MIN_VALUE : a[ca - 1], ar = ca == m ? Integer.MAX_VALUE : a[ca];
              int bl = cb == 0 ? Integer.MIN_VALUE : b[cb - 1], br = cb == n ? Integer.MAX_VALUE : b[cb];
              if (al > br) hi = ca - 1; else if (bl > ar) lo = ca + 1; else break; }
            worst = Math.max(worst, probes); }
          System.out.println("optimal: worst probes over 20000 max-size cases: " + worst); }

        // 5. Timing (median of 15 runs after warm-up), ns per call; skip with: ./run.sh --no-timing
        if (Arrays.asList(args).contains("--no-timing")) System.exit(fails == 0 ? 0 : 1);
        for (int size : new int[]{1_000, 100_000, 1_000_000}) {
            int[] a = sortedRandom(r, size, -M, M), b = sortedRandom(r, size, -M, M);
            StringBuilder line = new StringBuilder("m=n=" + size + ":");
            for (Impl im : IMPLS) {
                int reps = im.name().equals("brute") || im.name().equals("better")
                    ? Math.max(3, 2_000_000 / size) : 200_000;
                long[] runs = new long[15];
                double sink = 0;
                for (int w = 0; w < 3; w++)
                    for (int k = 0; k < reps; k++) sink += im.f().apply(a, b);
                for (int t = 0; t < runs.length; t++) {
                    long t0 = System.nanoTime();
                    for (int k = 0; k < reps; k++) sink += im.f().apply(a, b);
                    runs[t] = (System.nanoTime() - t0) / reps;
                }
                Arrays.sort(runs);
                line.append(String.format(" %s=%,dns", im.name(), runs[runs.length / 2]));
                if (sink == 42.4242) System.out.print("");
            }
            System.out.println(line);
        }
        System.exit(fails == 0 ? 0 : 1);
    }
}
