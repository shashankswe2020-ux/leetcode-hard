import java.util.Arrays;

// The 1,000-machine version: the array is split, in order,
// across machines (shards). Numbers never leave their machine.
// Once, up front, each machine sends two numbers: its biggest
// value and its total. Then, for every guess, the coordinator
// walks the machines in order carrying the open piece's weight:
// - a machine whose whole total still fits under the limit
//   costs no message: the coordinator adds its total locally;
// - only a machine where a cut must land gets one round trip:
//   (limit, carry) out, (cuts, new carry) back.
// The walk stops once it has counted more than k pieces.
final class ShardedSplit {

    // one machine: its own numbers, never shipped
    static final class Machine {
        final int[] nums;
        final long max, total;

        Machine(int[] nums) {
            this.nums = nums;
            long m = 0, t = 0;
            for (int v : nums) { m = Math.max(m, v); t += v; }
            max = m; total = t;
        }

        // one round trip: the open piece arrives weighing carry;
        // continue the greedy over this machine's numbers
        long[] continueFrom(long carry, long limit) {
            long cuts = 0, sum = carry;
            for (int num : nums) {
                if (sum + num > limit) { cuts++; sum = num; }
                else sum += num;
            }
            return new long[] {cuts, sum};
        }
    }

    // what the network carried (for the episode's numbers)
    static final class Traffic {
        long roundTrips, valuesShipped, probes, maxTripsPerProbe;
    }

    // pieces for this limit, capped at k + 1 (enough to decide)
    static long pieces(Machine[] machines, long limit, long k, Traffic t) {
        long count = 1, carry = 0, trips = 0;
        for (Machine m : machines) {
            if (carry + m.total <= limit) {          // fits whole: no message
                carry += m.total;
                continue;
            }
            long[] reply = m.continueFrom(carry, limit);
            trips++;
            count += reply[0];
            carry = reply[1];
            if (count > k) break;                    // already too many
        }
        if (t != null) {
            t.roundTrips += trips;
            t.valuesShipped += 4 * trips;            // 2 out, 2 back
            t.maxTripsPerProbe = Math.max(t.maxTripsPerProbe, trips);
        }
        return count;
    }


    // Batching: send B guesses in one walk. A message to a machine
    // carries (limit, carry) for every guess that still needs it,
    // so one walk answers B guesses: a (B + 1)-way search.
    // counts[j] = pieces for limits[j], each capped once > k.
    static long[] piecesBatch(Machine[] machines, long[] limits, long k, Traffic t) {
        int b = limits.length;
        long[] count = new long[b], carry = new long[b];
        Arrays.fill(count, 1);
        long trips = 0;
        for (Machine m : machines) {
            int asked = 0;
            for (int j = 0; j < b; j++) {
                if (count[j] > k) continue;                // decided: too many
                if (carry[j] + m.total <= limits[j]) {     // fits whole: local
                    carry[j] += m.total;
                    continue;
                }
                long[] reply = m.continueFrom(carry[j], limits[j]);
                count[j] += reply[0];
                carry[j] = reply[1];
                asked++;
            }
            if (asked > 0) {                               // one round trip
                trips++;
                if (t != null) t.valuesShipped += 4L * asked;
            }
        }
        if (t != null) {
            t.roundTrips += trips;
            t.maxTripsPerProbe = Math.max(t.maxTripsPerProbe, trips);
        }
        return count;
    }

    // (B + 1)-way search over [lo, hi]: each walk tests B limits
    static long splitArrayBatched(int[][] shards, int k, int b, Traffic t) {
        Machine[] machines = new Machine[shards.length];
        long lo = 0, hi = 0;
        for (int i = 0; i < shards.length; i++) {
            machines[i] = new Machine(shards[i]);
            lo = Math.max(lo, machines[i].max);
            hi += machines[i].total;
        }
        if (t != null) t.valuesShipped += 2L * shards.length;
        while (lo < hi) {
            long span = hi - lo;                           // candidates lo..hi-1
            int m = (int) Math.min(b, span);
            long[] limits = new long[m];
            for (int j = 0; j < m; j++)                    // evenly spread, increasing
                limits[j] = lo + (span * (j + 1)) / (m + 1);
            if (span <= b) for (int j = 0; j < m; j++) limits[j] = lo + j;
            if (t != null) t.probes++;
            long[] c = piecesBatch(machines, limits, k, t);
            long newLo = lo, newHi = hi;
            for (int j = 0; j < m; j++) {
                if (c[j] <= k) { newHi = limits[j]; break; }   // first yes
                newLo = limits[j] + 1;                         // a no
            }
            lo = newLo; hi = newHi;
        }
        return lo;
    }

    static long splitArray(int[][] shards, int k) {
        return splitArray(shards, k, null);
    }

    static long splitArray(int[][] shards, int k, Traffic t) {
        Machine[] machines = new Machine[shards.length];
        long lo = 0, hi = 0;
        for (int i = 0; i < shards.length; i++) {
            machines[i] = new Machine(shards[i]);
            lo = Math.max(lo, machines[i].max);
            hi += machines[i].total;
        }
        if (t != null) t.valuesShipped += 2L * shards.length;   // max + total, once
        while (lo < hi) {
            long mid = lo + (hi - lo) / 2;
            if (t != null) t.probes++;
            if (pieces(machines, mid, k, t) <= k) hi = mid; else lo = mid + 1;
        }
        return lo;    // a long: a billion numbers can overflow an int
    }
}
