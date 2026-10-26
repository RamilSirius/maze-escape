import java.util.*;

/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells(), cols = w.cols(), exit = n - 1;
        int[][] open = Graphs.open(w, broken);

        int deadEnds = 0;
        for (int[] doors : open) if (doors.length == 1) deadEnds++;

        // DFS with my own stack instead of recursion: the stack lives in an array on the heap,
        // so a maze of 1000 x 1000 cells cannot overflow the Java call stack.
        // stack[0..top] is the current way from the start; dir[i] is the next direction to try from stack[i].
        int[] step = {-cols, 1, cols, -1};   // up, right, down, left
        int[] stack = new int[n];
        int[] dir = new int[n];
        boolean[] marked = new boolean[n];
        int top = 0, visited = 1;
        stack[0] = 0;
        marked[0] = true;
        boolean found = exit == 0;
        while (!found && top >= 0) {
            int cell = stack[top];
            if (dir[top] == step.length) { top--; continue; }   // all four tried: go back
            int next = cell + step[dir[top]++];
            if (!isOpen(open[cell], next) || marked[next]) continue;
            marked[next] = true;
            visited++;
            stack[++top] = next;
            dir[top] = 0;
            found = next == exit;
        }
        // when the exit is marked, the stack holds exactly the way from the start to the exit
        int[] path = found ? Arrays.copyOf(stack, top + 1) : new int[0];
        return new Result(path, visited, deadEnds);
    }

    /** True if 'next' is in the list of open neighbours (at most four cells). */
    private static boolean isOpen(int[] doors, int next) {
        for (int d : doors) if (d == next) return true;
        return false;
    }
}
