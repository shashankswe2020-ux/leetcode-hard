import java.util.*;

// Day 003 harness: LeetCode 410 Split Array Largest Sum.
// ./run.sh [--no-timing] [seed]
public class Harness {
    static int failures = 0;

    static void check(boolean ok, String what) {
        if (!ok) { failures++; System.out.println("FAIL " + what); }
    }

    // ---------- independent reference: every way to cut, in long math
    static long reference(int[] a, int k) {
        return refRec(a, 0, k);
    }

    static long refRec(int[] a, int start, int k) {
        int n = a.length;
        if (k == 1) {
            long s = 0;
            for (int i = start; i < n; i++) s += a[i];
            return s;
        }
        long best = Long.MAX_VALUE, first = 0;
        for (int end = start; end <= n - k; end++) {
            first += a[end];
            best = Math.min(best, Math.max(first, refRec(a, end + 1, k - 1)));
        }
        return best;
    }

    // ---------- every solution under test
    static final String[] NAMES = {"brute", "dp", "dpfast", "optimal", "modern", "longOnly", "sharded"};

    static long run(String name, int[] a, int k, Random rnd) {
        switch (name) {
            case "brute": return new Brute().splitArray(a.clone(), k);
            case "dp": return new Dp().splitArray(a.clone(), k);
            case "dpfast": return new DpFast().splitArray(a.clone(), k);
            case "optimal": return new Optimal().splitArray(a.clone(), k);
            case "modern": return new Modern().splitArray(a.clone(), k);
            case "longOnly": return Variants.longOnly(a.clone(), k);
            case "sharded": return ShardedSplit.splitArray(randomShards(a, rnd), k);
            default: throw new IllegalArgumentException(name);
        }
    }

    static int[][] randomShards(int[] a, Random rnd) {
        int s = 1 + rnd.nextInt(Math.max(1, Math.min(8, a.length)));
        List<int[]> out = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < s; i++) {
            int end = (i == s - 1) ? a.length : start + rnd.nextInt(a.length - start + 1);
            out.add(Arrays.copyOfRange(a, start, end));     // empty shards allowed
            start = end;
        }
        return out.toArray(new int[0][]);
    }

    // ---------- the episode's probes (same logic as optimal/Solution.java)
    static int pieces(int[] nums, int limit) {
        int count = 1, sum = 0;
        for (int num : nums) {
            if (sum + num > limit) { count++; sum = num; } else sum += num;
        }
        return count;
    }

    // ---------- bug versions (each prints its wrong answer)
    static int bugLoZero(int[] nums, int k) {           // lo starts at 0, not the max
        int lo = 0, hi = 0;
        for (int num : nums) hi += num;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (pieces(nums, mid) <= k) hi = mid; else lo = mid + 1;
        }
        return lo;
    }

    static int bugCountFromZero(int[] nums, int k) {    // forgets the first piece
        int lo = 0, hi = 0;
        for (int num : nums) { lo = Math.max(lo, num); hi += num; }
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            int count = 0, sum = 0;
            for (int num : nums) {
                if (sum + num > mid) { count++; sum = num; } else sum += num;
            }
            if (count <= k) hi = mid; else lo = mid + 1;
        }
        return lo;
    }

    static int bugLoEqualsMid(int[] nums, int k, int[] iters) {   // lo = mid: never ends
        int lo = 0, hi = 0;
        for (int num : nums) { lo = Math.max(lo, num); hi += num; }
        int it = 0;
        while (lo < hi && it < 1000) {
            it++;
            int mid = lo + (hi - lo) / 2;
            if (pieces(nums, mid) <= k) hi = mid; else lo = mid;
        }
        iters[0] = it;
        return lo;
    }

    static int bugMidSum(int[] nums, int k, int[] iters) {        // (lo + hi) / 2 overflows
        int lo = 0, hi = 0;
        for (int num : nums) { lo = Math.max(lo, num); hi += num; }
        int it = 0;
        while (lo < hi && it < 1000) {
            it++;
            int mid = (lo + hi) / 2;
            if (pieces(nums, mid) <= k) hi = mid; else lo = mid + 1;
        }
        iters[0] = it;
        return lo;
    }

    static int bugGreaterEqual(int[] nums, int k) {     // cuts when the piece would EQUAL the limit
        int lo = 0, hi = 0;
        for (int num : nums) { lo = Math.max(lo, num); hi += num; }
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            int count = 1, sum = 0;
            for (int num : nums) {
                if (sum + num >= mid) { count++; sum = num; } else sum += num;
            }
            if (count <= k) hi = mid; else lo = mid + 1;
        }
        return lo;
    }

    // ---------- homework reference
    static int kokoBrute(int[] piles, int h) {
        for (int speed = 1; ; speed++) {
            long t = 0;
            for (int p : piles) t += (p + speed - 1) / speed;
            if (t <= h) return speed;
        }
    }

    static long median(long[] xs) { long[] c = xs.clone(); Arrays.sort(c); return c[c.length / 2]; }

    interface Job { long go(); }

    static long timeNs(Job j, int reps) {
        long sink = 0;
        // warm-up: at least 20,000 calls or 2 s, whichever comes first
        long w0 = System.nanoTime(); int warm = 0;
        while (warm < 20_000 && System.nanoTime() - w0 < 2_000_000_000L) { sink += j.go(); warm++; }
        // batch fast jobs so each timed sample is at least ~2 ms
        long one = Math.max(1, (System.nanoTime() - w0) / Math.max(1, warm));
        int batch = (int) Math.max(1, Math.min(100_000, 2_000_000 / one));
        long[] t = new long[reps];
        for (int r = 0; r < reps; r++) {
            long t0 = System.nanoTime();
            for (int b = 0; b < batch; b++) sink += j.go();
            t[r] = (System.nanoTime() - t0) / batch;
        }
        if (sink == 42) System.out.print("");
        return median(t);
    }

    static String human(long ns) {
        if (ns >= 1_000_000_000L) return String.format("%.2f s", ns / 1e9);
        if (ns >= 1_000_000) return String.format("%.1f ms", ns / 1e6);
        if (ns >= 1_000) return String.format("%.1f µs", ns / 1e3);
        return ns + " ns";
    }

    public static void main(String[] args) throws Exception {
        boolean timing = true;
        long seed = 20261105L;
        for (String a : args) {
            if (a.equals("--no-timing")) timing = false;
            else if (!a.startsWith("-")) seed = Long.parseLong(a);
        }
        System.out.println("JVM " + System.getProperty("java.version") + " · seed " + seed);
        Random rnd = new Random(seed);

        // 1. the episode example
        int[] ex = {4, 9, 3, 6, 2, 7, 5};
        System.out.println("\n1. example [4, 9, 3, 6, 2, 7, 5], k = 3");
        for (String nm : NAMES) {
            long got = run(nm, ex, 3, rnd);
            System.out.println("   " + nm + " = " + got);
            check(got == 13, "example " + nm);
        }
        int lo = 9, hi = 36, probe = 0;
        StringBuilder trace = new StringBuilder();
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2, p = pieces(ex, mid);
            probe++;
            trace.append(String.format("   probe %d: lo %d hi %d mid %d -> %d pieces -> %s%n",
                    probe, lo, hi, mid, p, p <= 3 ? "fits, hi = mid" : "too many, lo = mid + 1"));
            if (p <= 3) hi = mid; else lo = mid + 1;
        }
        System.out.print(trace);
        System.out.println("   answer " + lo + " after " + probe + " probes; the cut: 4 9 | 3 6 2 | 7 5 = 13, 11, 12");
        check(lo == 13 && probe == 5, "probe trace");
        System.out.print("   pieces(x) for x = 9..16:");
        for (int x = 9; x <= 16; x++) System.out.print(" " + x + ":" + pieces(ex, x));
        System.out.println();

        // 2. edge cases
        System.out.println("\n2. edge cases");
        int[][] edges = {{7}, {0}, {0, 0, 0}, {5, 5, 5, 5}, {1, 2, 3, 4, 5}, {5, 4, 3, 2, 1},
                {1000000}, {1000000, 1000000, 1000000}, {0, 9, 0}, {2, 3, 1, 2, 4, 3}};
        int edgeCount = 0;
        for (int[] e : edges) {
            for (int k = 1; k <= e.length; k++) {
                long want = reference(e, k);
                for (String nm : NAMES) check(run(nm, e, k, rnd) == want, "edge " + Arrays.toString(e) + " k=" + k + " " + nm);
                edgeCount++;
            }
        }
        System.out.println("   " + edgeCount + " (array, k) edge cases x " + NAMES.length + " solutions: all agree");

        // 3. random differential vs the exhaustive reference
        System.out.println("\n3. random differential (seed " + seed + ")");
        int cases = 10_000;
        for (int c = 0; c < cases; c++) {
            int n = 1 + rnd.nextInt(12);
            int k = 1 + rnd.nextInt(n);
            int max = rnd.nextInt(4) == 0 ? 1_000_000 : (rnd.nextInt(3) == 0 ? 3 : 20);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(max + 1);
            long want = reference(a, k);
            for (String nm : NAMES) {
                long got = run(nm, a, k, rnd);
                if (got != want) { check(false, "random " + Arrays.toString(a) + " k=" + k + " " + nm + " got " + got + " want " + want); break; }
            }
        }
        System.out.println("   " + cases + " random inputs (n 1..12, values up to 20 or 10^6) x " + NAMES.length + " solutions vs every-cut reference: all agree");

        // 4. limit-size cases (reference too slow there: compare with the DP)
        int big = 300;
        for (int c = 0; c < big; c++) {
            int n = 1 + rnd.nextInt(1000);
            int k = 1 + rnd.nextInt(Math.min(50, n));
            int[] a = new int[n];
            int mx = rnd.nextBoolean() ? 1_000_000 : 1 + rnd.nextInt(1000);
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(mx + 1);
            long want = new Dp().splitArray(a.clone(), k);
            for (String nm : new String[] {"dpfast", "optimal", "modern", "longOnly", "sharded"})
                check(run(nm, a, k, rnd) == want, "big n=" + n + " k=" + k + " " + nm);
        }
        int[] worst = new int[1000];
        Arrays.fill(worst, 1_000_000);
        for (String nm : new String[] {"dp", "dpfast", "optimal", "modern", "sharded"})
            check(run(nm, worst, 1, rnd) == 1_000_000_000L, "all 10^6, k=1 " + nm);
        System.out.println("   " + big + " random inputs up to n = 1,000, k = 50, values to 10^6: dpfast, optimal, modern, sharded match the DP");
        System.out.println("   1,000 x 10^6 with k = 1 -> 1,000,000,000 (int max is 2,147,483,647: the sum uses 47% of an int)");

        // 5. the sharded probe: the coordinator's walk vs the sequential greedy, every limit
        int shardChecks = 0;
        for (int c = 0; c < 2000; c++) {
            int n = 1 + rnd.nextInt(40);
            int[] a = new int[n];
            int mx = rnd.nextInt(3) == 0 ? 2 : 30;
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(mx + 1);
            int[][] sh = randomShards(a, rnd);
            ShardedSplit.Machine[] ms = new ShardedSplit.Machine[sh.length];
            for (int i = 0; i < sh.length; i++) ms[i] = new ShardedSplit.Machine(sh[i]);
            int mxv = 0; long sum = 0;
            for (int v : a) { mxv = Math.max(mxv, v); sum += v; }
            for (long limit = Math.max(mxv, 1); limit <= sum + 1; limit++) {
                long seq = Variants.count(a, limit);
                check(ShardedSplit.pieces(ms, limit, Long.MAX_VALUE / 2, null) == seq, "shard walk " + Arrays.toString(a) + " limit " + limit);
                int kk = 1 + rnd.nextInt(n);
                check(ShardedSplit.pieces(ms, limit, kk, null) <= kk == (seq <= kk), "shard decide " + Arrays.toString(a) + " limit " + limit);
                shardChecks += 2;
            }
        }
        System.out.println("\n4. sharded probe: " + shardChecks + " checks (exact count, and the capped fits-in-k decision) = sequential greedy");

        // 6. the bugs people ship
        System.out.println("\n5. bugs");
        int[] b1 = {1, 9};
        System.out.println("   lo = 0 instead of the max: [1, 9], k = 2 -> " + bugLoZero(b1, 2) + " (truth 9)");
        check(bugLoZero(b1, 2) != 9, "bug lo=0 reproduces");
        int b1ex = bugLoZero(ex, 3);
        System.out.println("   (on the episode example it happens to give " + b1ex + ")");
        int wrongLo = 0;
        for (int c = 0; c < cases; c++) {
            int n = 1 + rnd.nextInt(10), k = 1 + rnd.nextInt(n);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(21);
            if (bugLoZero(a, k) != reference(a, k)) wrongLo++;
        }
        System.out.println("   lo = 0: wrong on " + wrongLo + " of " + cases + " random inputs");
        System.out.println("   count starts at 0: example -> " + bugCountFromZero(ex, 3) + " (truth 13)");
        check(bugCountFromZero(ex, 3) != 13, "bug count=0 reproduces");
        int[] it = new int[1];
        int r = bugLoEqualsMid(ex, 3, it);
        System.out.println("   lo = mid (no + 1): still looping after " + it[0] + " rounds, stuck at lo " + r);
        check(it[0] == 1000, "bug lo=mid loops");
        System.out.println("   >= instead of >: example -> " + bugGreaterEqual(ex, 3) + " (truth 13)");
        check(bugGreaterEqual(ex, 3) != 13, "bug >= reproduces");
        // overflow: honest limits
        int[] lim = new int[1000];
        Arrays.fill(lim, 1_000_000);
        bugMidSum(lim, 1, it);
        System.out.println("   (lo + hi) / 2 at LeetCode's limit (1,000 x 10^6, k = 1): " + bugMidSum(lim, 1, it)
                + " in " + it[0] + " rounds -> correct; lo + hi peaks at 2,000,000,000, 7% below int max (it overflows from 1,074 numbers)");
        int[] over = new int[1500];
        Arrays.fill(over, 1_000_000);
        int bad = bugMidSum(over, 1, it);
        System.out.println("   (lo + hi) / 2 at 1,500 x 10^6, k = 1: " + bad + " after " + it[0] + " rounds (truth 1,500,000,000) -> overflow");
        check(bad != 1_500_000_000, "bug mid overflow reproduces");
        System.out.println("   lo + (hi - lo) / 2 on the same input: " + new Optimal().splitArray(over, 1));
        check(new Optimal().splitArray(over, 1) == 1_500_000_000, "safe mid at 1500");
        int[] over2 = new int[3000];
        Arrays.fill(over2, 1_000_000);
        int intSum = 0;
        for (int v : over2) intSum += v;
        System.out.println("   int sum of 3,000 x 10^6: " + intSum + " (truth 3,000,000,000); sharded (long) -> "
                + ShardedSplit.splitArray(new int[][] {over2}, 1));
        check(intSum < 0, "int sum overflows at 3000");

        // 6b. outside the constraints: negative numbers break the bounds and the greedy
        int[] neg = {2, -2};
        System.out.println("   negatives (not allowed by LeetCode): [2, -2], k = 1 -> binary search says " + new Optimal().splitArray(neg, 1) + " (truth 0: the one piece weighs 0)");
        check(new Optimal().splitArray(neg, 1) == 2, "negatives break optimal");

        // 6c. follow-up: search the candidate run sums instead of the range
        int cand = 0;
        int[] pc = new int[1];
        for (int c = 0; c < 3000; c++) {
            int n = 1 + rnd.nextInt(12), k = 1 + rnd.nextInt(n);
            int[] a = new int[n];
            int mx = rnd.nextBoolean() ? 1_000_000 : 20;
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(mx + 1);
            check(CandidateSearch.splitArray(a, k, pc) == reference(a, k), "candidates " + Arrays.toString(a) + " k=" + k);
            cand++;
        }
        System.out.println("   candidate-sum search: " + cand + " random inputs vs every-cut reference: all agree");

        // 6d. follow-up: doubles, candidate run weights added left to right
        int dbl = 0;
        for (int c = 0; c < 3000; c++) {
            int n = 1 + rnd.nextInt(10), k = 1 + rnd.nextInt(n);
            double[] a = new double[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(4) == 0 ? rnd.nextInt(5) : rnd.nextDouble() * (rnd.nextBoolean() ? 1e6 : 1);
            double got = DoubleCandidates.splitArray(a, k), want = DoubleCandidates.reference(a, k);
            check(Double.compare(got, want) == 0, "doubles " + Arrays.toString(a) + " k=" + k + " got " + got + " want " + want);
            dbl++;
        }
        System.out.println("   doubles, candidate run weights: " + dbl + " random inputs vs every-cut reference (same left-to-right adds): exactly equal");

        // 6e. sharded, batched guesses: B limits per walk, a (B + 1)-way search
        int batched = 0;
        for (int c = 0; c < 4000; c++) {
            int n = 1 + rnd.nextInt(40), k = 1 + rnd.nextInt(n);
            int[] a = new int[n];
            int mx = rnd.nextInt(3) == 0 ? 2 : (rnd.nextBoolean() ? 30 : 1_000_000);
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(mx + 1);
            int[][] sh = randomShards(a, rnd);
            long want = ShardedSplit.splitArray(sh, k);
            if (n <= 12) check(want == reference(a, k), "sharded vs reference " + Arrays.toString(a));
            else check(want == new Dp().splitArray(a, k), "sharded vs DP " + Arrays.toString(a));
            for (int b : new int[] {1, 2, 3, 7, 15}) {
                check(ShardedSplit.splitArrayBatched(sh, k, b, null) == want, "batched B=" + b + " " + Arrays.toString(a) + " k=" + k);
                batched++;
            }
            ShardedSplit.Machine[] ms = new ShardedSplit.Machine[sh.length];
            for (int i = 0; i < sh.length; i++) ms[i] = new ShardedSplit.Machine(sh[i]);
            long[] lims = new long[1 + rnd.nextInt(15)];
            for (int j = 0; j < lims.length; j++) lims[j] = rnd.nextInt((int) Math.min(Integer.MAX_VALUE, 40L * (mx + 1)) + 1);
            long[] got = ShardedSplit.piecesBatch(ms, lims, k, null);
            for (int j = 0; j < lims.length; j++) {
                long seq = Variants.count(a, lims[j]);
                check((got[j] <= k) == (seq <= k), "batch decide " + Arrays.toString(a) + " limit " + lims[j]);
                batched++;
            }
        }
        System.out.println("   sharded, batched guesses (B = 1, 2, 3, 7, 15): " + batched + " checks (answers and per-limit decisions) = sequential");

        // 7. homework: LeetCode 875
        int hw = 0;
        for (int c = 0; c < 5000; c++) {
            int n = 1 + rnd.nextInt(6);
            int[] piles = new int[n];
            int mx = 0;
            for (int i = 0; i < n; i++) { piles[i] = 1 + rnd.nextInt(30); mx = Math.max(mx, piles[i]); }
            int h = n + rnd.nextInt(40);
            check(new Koko().minEatingSpeed(piles, h) == kokoBrute(piles, h), "koko " + Arrays.toString(piles) + " h " + h);
            hw++;
        }
        int[] kbig = new int[10_000];
        Arrays.fill(kbig, 1_000_000_000);
        int kspeed = new Koko().minEatingSpeed(kbig, 1_000_000_000);
        System.out.println("\n6. homework (LeetCode 875): " + hw + " random cases vs trying every speed: all agree; 10^4 piles of 10^9, h = 10^9 -> speed " + kspeed);
        check(kspeed == 10_000, "koko big");

        // 8. step counts at the limit
        System.out.println("\n7. step counts at the limit (n = 1,000, k = 50)");
        Random rs = new Random(7);
        int[] L = new int[1000];
        for (int i = 0; i < L.length; i++) L[i] = rs.nextInt(1_000_001);
        long dpCells = 0;
        for (int j = 2; j <= 50; j++) for (int i = j; i <= 1000; i++) dpCells += i - j + 1;
        long fastCells = 0;
        {
            int n = L.length; int[] pre = new int[n + 1];
            for (int i = 0; i < n; i++) pre[i + 1] = pre[i] + L[i];
            int[] best = pre.clone();
            for (int j = 2; j <= 50; j++) {
                int[] next = new int[n + 1];
                for (int i = j; i <= n; i++) {
                    int low = Integer.MAX_VALUE;
                    for (int cut = i - 1; cut >= j - 1; cut--) {
                        int last = pre[i] - pre[cut];
                        fastCells++;
                        if (last >= low) break;
                        low = Math.min(low, Math.max(best[cut], last));
                    }
                    next[i] = low;
                }
                best = next;
            }
        }
        int pr = Variants.probes(L, 50, false), prBound = Variants.probes(L, 50, true);
        System.out.println("   DP inner steps: " + dpCells + " · with the early break: " + fastCells + " (this input)");
        System.out.println("   binary search probes: " + pr + " (" + (pr * 1000L) + " greedy steps) · with the ceilDiv average bound: " + prBound);
        int maxProbes = 64 - Long.numberOfLeadingZeros(1_000_000_000L - 1_000_000L);
        System.out.println("   worst case probes: ceil(log2(10^9 - 10^6 + 1)) = " + maxProbes);
        int prWorst = Variants.probes(worst, 50, false), prWorstB = Variants.probes(worst, 50, true);
        System.out.println("   all 10^6, k = 50: probes " + prWorst + " · with the bound " + prWorstB);
        int[] pcL = new int[1];
        long candAns = CandidateSearch.splitArray(L, 50, pcL);
        check(candAns == new Optimal().splitArray(L, 50), "candidates at the limit");
        System.out.println("   candidate-sum search at the limit: " + (1000L * 1001 / 2) + " run sums, " + pcL[0] + " probes, answer " + candAns + " (= binary search)");

        if (failures > 0) { System.out.println("\nFAILURES: " + failures); System.exit(1); }
        System.out.println("\nall correctness checks passed");
        if (!timing) return;

        // 9. timings
        System.out.println("\n8. timings (median of 15 after warm-up)");
        Optimal opt = new Optimal(); Modern mod = new Modern(); Dp dp = new Dp(); DpFast dpf = new DpFast();
        long tOpt = timeNs(() -> opt.splitArray(L, 50), 15);
        long tMod = timeNs(() -> mod.splitArray(L, 50), 15);
        long tLong = timeNs(() -> Variants.longOnly(L, 50), 15);
        long tDp = timeNs(() -> dp.splitArray(L, 50), 15);
        long tDpf = timeNs(() -> dpf.splitArray(L, 50), 15);
        System.out.println("   n = 1,000, k = 50, random values to 10^6:");
        System.out.println("   DP: " + human(tDp) + " · DP with break: " + human(tDpf) + " · binary search: " + human(tOpt)
                + " · long, no tricks: " + human(tLong) + " · modern (long sums, checked return): " + human(tMod));
        long tWO = timeNs(() -> opt.splitArray(worst, 50), 15), tWM = timeNs(() -> mod.splitArray(worst, 50), 15);
        System.out.println("   all 10^6, k = 50: binary search " + human(tWO) + " · modern " + human(tWM));
        long tSL = timeNs(() -> Variants.setupLoop(L)[1], 15), tSS = timeNs(() -> Variants.setupStream(L)[1], 15);
        System.out.println("   setup only (max and sum), long warm-up: loop " + human(tSL) + " · Arrays.stream twice " + human(tSS) + String.format(" (x%.2f)", (double) tSS / tSL));
        // brute: measure small, extrapolate by the number of cut choices
        int[] sm = Arrays.copyOf(L, 24);
        Brute br = new Brute();
        long tB = timeNs(() -> br.splitArray(sm, 6), 5);
        double ways = 33649.0;     // C(23, 5)
        double perWay = tB / ways;
        System.out.println("   brute force n = 24, k = 6 (" + (long) ways + " ways): " + human(tB) + " = " + String.format("%.1f", perWay) + " ns per way");
        java.math.BigInteger c = java.math.BigInteger.ONE;
        for (int i = 0; i < 49; i++) c = c.multiply(java.math.BigInteger.valueOf(999 - i)).divide(java.math.BigInteger.valueOf(i + 1));
        double cw = c.doubleValue();
        System.out.println("   at n = 1,000, k = 50: C(999, 49) = " + String.format("%.2e", cw) + " ways (" + c.toString().length() + " digits) -> about "
                + String.format("%.1e", perWay * cw / 1e9 / 3.156e7) + " years (extrapolated)");
        // sharded: a million numbers on 1,000 shards
        int[][] shards = new int[1000][1000];
        Random rsh = new Random(11);
        for (int[] s : shards) for (int i = 0; i < s.length; i++) s[i] = rsh.nextInt(1_000_001);
        long tSh = timeNs(() -> ShardedSplit.splitArray(shards, 50), 5);
        ShardedSplit.Traffic tr = new ShardedSplit.Traffic();
        long ans = ShardedSplit.splitArray(shards, 50, tr);
        System.out.println("   network for the whole answer: " + tr.probes + " guesses · " + tr.roundTrips + " round trips (at most "
                + tr.maxTripsPerProbe + " per guess, one after another) · " + tr.valuesShipped + " values shipped (2,000 up front, 4 per round trip) vs 1,000,000 numbers that never move");
        for (int b : new int[] {15}) {
            ShardedSplit.Traffic tb = new ShardedSplit.Traffic();
            long ab = ShardedSplit.splitArrayBatched(shards, 50, b, tb);
            check(ab == ans, "batched at scale");
            System.out.println("   batched, B = " + b + " guesses per walk: " + tb.probes + " walks · " + tb.roundTrips + " round trips (at most "
                    + tb.maxTripsPerProbe + " per walk) · " + tb.valuesShipped + " values shipped · answer " + ab + " (= unbatched)");
        }
        int[] flat = new int[1_000_000];
        for (int i = 0; i < 1000; i++) System.arraycopy(shards[i], 0, flat, i * 1000, 1000);
        Modern m2 = new Modern();
        long tFlat = timeNs(() -> {
            long lo2 = 0, hi2 = 0;
            for (int v : flat) { lo2 = Math.max(lo2, v); hi2 += v; }
            while (lo2 < hi2) { long mid = lo2 + (hi2 - lo2) / 2; if (Variants.count(flat, mid) <= 50) hi2 = mid; else lo2 = mid + 1; }
            return lo2;
        }, 5);
        if (m2 == null) System.out.print("");
        System.out.println("   10^6 numbers on 1,000 shards, k = 50: answer " + ans + " (needs a long) · sharded (simulated, " + Runtime.getRuntime().availableProcessors() + " cores) " + human(tSh) + " · one machine " + human(tFlat));
    }
}
