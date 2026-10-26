import java.util.*;

/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] out = Graphs.slidesFrom(s), in = Graphs.slidesInto(s);
        int[] stack = new int[n];   // every cell goes on the stack at most once in a pass, so n is enough

        // Pass 1: DFS on the slides, order[] gets the cells in the order they are finished.
        boolean[] seen = new boolean[n];
        int[] nextSlide = new int[n];   // nextSlide[u]: how many slides of u are already tried
        int[] order = new int[n];
        int finished = 0;
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;
            int top = 0;
            stack[0] = start;
            seen[start] = true;
            while (top >= 0) {
                int u = stack[top];
                if (nextSlide[u] < out[u].length) {
                    int v = s.to()[out[u][nextSlide[u]++]];
                    if (!seen[v]) { seen[v] = true; stack[++top] = v; }
                } else {
                    order[finished++] = u;   // all slides of u are done: u is finished
                    top--;
                }
            }
        }

        // Pass 2: from the last finished cell to the first, DFS on the reversed slides.
        // Everything one DFS reaches and that has no zone yet is one zone.
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zones = 0;
        for (int i = n - 1; i >= 0; i--) {
            int root = order[i];
            if (zoneOf[root] != -1) continue;
            int top = 0;
            stack[0] = root;
            zoneOf[root] = zones;
            while (top >= 0) {
                int v = stack[top--];
                for (int k : in[v]) {
                    int u = s.from()[k];
                    if (zoneOf[u] == -1) { zoneOf[u] = zones; stack[++top] = u; }
                }
            }
            zones++;
        }
        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        if (zoneCount == 0) return new long[]{0, 0};

        // The graph of zones: leaving[X] holds the slides that go from zone X into another zone.
        // Two slides from X to Y are two parallel edges; the cheaper one wins when we relax them.
        int[] count = new int[zoneCount];
        for (int i = 0; i < s.count(); i++) {
            if (zoneOf[s.from()[i]] != zoneOf[s.to()[i]]) count[zoneOf[s.from()[i]]]++;
        }
        int[][] leaving = new int[zoneCount][];
        for (int z = 0; z < zoneCount; z++) leaving[z] = new int[count[z]];
        int[] filled = new int[zoneCount];
        for (int i = 0; i < s.count(); i++) {
            int x = zoneOf[s.from()[i]];
            if (x != zoneOf[s.to()[i]]) leaving[x][filled[x]++] = i;
        }

        // Zone numbers are a topological order: every edge goes from a smaller number to a larger one.
        // So when we reach zone X, all edges into X are already relaxed and chain[X], cost[X] are final.
        long[] chain = new long[zoneCount];   // zones on the longest chain that ends in X
        long[] cost = new long[zoneCount];    // cheapest time from the zone of the start to X
        Arrays.fill(chain, 1);
        Arrays.fill(cost, Long.MAX_VALUE);
        int from = zoneOf[0], to = zoneOf[s.n() - 1];
        cost[from] = 0;
        long stages = 0;
        for (int x = 0; x < zoneCount; x++) {
            stages = Math.max(stages, chain[x]);
            for (int i : leaving[x]) {
                int y = zoneOf[s.to()[i]];
                chain[y] = Math.max(chain[y], chain[x] + 1);
                if (cost[x] != Long.MAX_VALUE) cost[y] = Math.min(cost[y], cost[x] + s.time()[i]);
            }
        }
        return new long[]{stages, cost[to] == Long.MAX_VALUE ? -1 : cost[to]};
    }
}
