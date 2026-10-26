import java.util.*;

/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        int[] order = new int[w.count()];
        for (int k = 0; k < order.length; k++) order[k] = k;
        return kruskal(w, order);
    }

    /** Goes through the walls in the given order and breaks a wall when it joins two different sets. */
    private static int[] kruskal(Input.Walls w, int[] order) {
        DisjointSet dsu = new DisjointSet(w.cells());
        // a spanning tree of n cells has n - 1 edges, so this is the exact size of the answer
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int count = 0;
        for (int k : order) {
            if (count == broken.length) break;  // all cells are already in one set
            if (dsu.union(w.a()[k], w.b()[k])) broken[count++] = k;
        }
        return count == broken.length ? broken : Arrays.copyOf(broken, count);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] byCost = new Integer[w.count()];
        for (int k = 0; k < byCost.length; k++) byCost[k] = k;
        Arrays.sort(byCost, Comparator.comparingInt(k -> w.cost()[k]));
        int[] order = new int[byCost.length];
        for (int i = 0; i < order.length; i++) order[i] = byCost[i];
        return kruskal(w, order);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n == 0) return 0;
        int[][] wallsOf = wallsOfCells(w);
        boolean[] inTree = new boolean[n];
        // the queue holds wall indexes, the cheapest wall comes out first
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(k -> w.cost()[k]));
        long total = 0;
        int treeSize = 1;
        inTree[0] = true;
        for (int k : wallsOf[0]) pq.add(k);
        while (!pq.isEmpty() && treeSize < n) {
            int k = pq.poll();
            int a = w.a()[k], b = w.b()[k];
            if (inTree[a] && inTree[b]) continue;   // an old wall inside the tree: skip it
            int cell = inTree[a] ? b : a;            // the new cell behind the wall
            inTree[cell] = true;
            treeSize++;
            total += w.cost()[k];
            for (int next : wallsOf[cell]) {
                if (!inTree[w.a()[next]] || !inTree[w.b()[next]]) pq.add(next);
            }
        }
        return total;
    }

    /** wallsOf[cell] holds the indexes of the walls around this cell. */
    private static int[][] wallsOfCells(Input.Walls w) {
        int n = w.cells();
        int[] degree = new int[n];
        for (int k = 0; k < w.count(); k++) { degree[w.a()[k]]++; degree[w.b()[k]]++; }
        int[][] wallsOf = new int[n][];
        for (int i = 0; i < n; i++) wallsOf[i] = new int[degree[i]];
        int[] filled = new int[n];
        for (int k = 0; k < w.count(); k++) {
            int a = w.a()[k], b = w.b()[k];
            wallsOf[a][filled[a]++] = k;
            wallsOf[b][filled[b]++] = k;
        }
        return wallsOf;
    }
}
