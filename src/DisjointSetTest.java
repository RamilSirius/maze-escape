/**
 * Checks of Mission 0. Run:  java -cp out DisjointSetTest
 * Add your own checks below: a large chain, a union of a set with itself, count after many unions.
 */
public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        // a union of a set with itself changes nothing
        check(!d.union(4, 4), "union(4, 4) returns false");
        check(d.count() == 3, "still 3 sets after union(4, 4)");
        check(!d.union(0, 3), "union(0, 3) returns false: already one set");

        // a large chain: 0-1, 1-2, ..., joined one by one
        int n = 1_000_000;
        DisjointSet chain = new DisjointSet(n);
        boolean allJoined = true;
        for (int i = 0; i + 1 < n; i++) allJoined &= chain.union(i, i + 1);
        check(allJoined, "every union in a chain of 1 000 000 returns true");
        check(chain.count() == 1, "the chain is one set");
        check(chain.connected(0, n - 1), "the two ends of the chain are connected");
        check(chain.find(0) == chain.find(n - 1), "the two ends of the chain have the same root");

        // count after many unions: join cells with the same remainder mod 10
        DisjointSet mod = new DisjointSet(100);
        for (int i = 10; i < 100; i++) mod.union(i, i - 10);
        check(mod.count() == 10, "10 sets: one for every remainder mod 10");
        check(mod.connected(3, 93) && !mod.connected(3, 94), "3 and 93 together, 3 and 94 apart");
        for (int i = 1; i < 10; i++) mod.union(i, i - 1);
        check(mod.count() == 1, "one set after joining the remainders");
        check(!mod.union(57, 12), "union inside one set returns false");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
    }
}
