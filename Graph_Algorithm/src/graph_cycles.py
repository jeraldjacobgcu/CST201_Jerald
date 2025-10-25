from math import inf

# ---------- Configuration ----------
# Sample matrix (use inf for no-edge). You can replace this with a file loader.
# Example with negative edges allowed.
SAMPLE_MATRIX = [
    [0,   3,  inf,  inf, 1],
    [inf,  0, 6,  inf, 3],
    [ 1,   inf, 0,  inf, inf],
    [-4, inf, 5, 0, inf],
    [inf,   inf,   2, 2,  inf],
]

# ---------- Optional file input (uncomment to use) ----------
def load_matrix_from_file(path="matrix.txt"):
    """
    Reads a whitespace-separated matrix. Use 'inf' or a large number to denote no-edge.
    Returns an n x n matrix of floats (inf for missing edges).
    """
    data = []
    with open(path, "r") as f:
        for line in f:
            row = []
            for tok in line.strip().split():
                if tok.lower() == "inf":
                    row.append(inf)
                else:
                    row.append(float(tok))
            if row:
                data.append(row)
    # Basic validation (square matrix)
    n = len(data)
    assert all(len(r) == n for r in data), "Matrix must be square."
    return data

# MATRIX = load_matrix_from_file()  # <- Uncomment to load from file
MATRIX = SAMPLE_MATRIX

# ---------- Build adjacency from matrix ----------
def build_adjacency(W, allowed_vertices):
    A = {v: [] for v in allowed_vertices}
    for i in allowed_vertices:
        for j in allowed_vertices:
            if i != j and W[i][j] != inf:
                A[i].append(j)
    return A

# ---------- Johnson’s algorithm for simple cycle enumeration ----------
def find_min_weight_cycles(W):
    n = len(W)
    # bookkeeping for counting (optional)
    counters = {
        "comparisons": 0,   # condition checks
        "data_exchanges": 0 # pushes/pops + set/list add/removes
    }

    min_weight = None
    min_cycles = []

    blocked = [False] * n
    B = [set() for _ in range(n)]
    stack = []

    def cycle_weight(path):
        """Sum W[path[i]][path[i+1]] including last->first edge."""
        total = 0.0
        for i in range(len(path) - 1):
            total += W[path[i]][path[i+1]]
        return total

    def unblock(u):
        blocked[u] = False
        counters["data_exchanges"] += 1  # write to blocked
        while B[u]:
            x = B[u].pop()
            counters["data_exchanges"] += 1  # pop from B[u]
            if blocked[x]:
                unblock(x)

    def circuit(v, s, A):
        nonlocal min_weight, min_cycles
        found_cycle = False
        stack.append(v)
        counters["data_exchanges"] += 1  # push
        blocked[v] = True
        counters["data_exchanges"] += 1  # write to blocked

        for w in A.get(v, []):
            counters["comparisons"] += 1
            if w == s:
                # Found a cycle: stack + [s]
                path = stack + [s]
                wt = cycle_weight(path)
                if (min_weight is None) or (wt < min_weight):
                    min_weight = wt
                    min_cycles = [path[:]]
                elif wt == min_weight:
                    min_cycles.append(path[:])
                found_cycle = True
            elif not blocked[w]:
                counters["comparisons"] += 1
                if circuit(w, s, A):
                    found_cycle = True

        if found_cycle:
            unblock(v)
        else:
            for w in A.get(v, []):
                if v not in B[w]:
                    B[w].add(v)
                    counters["data_exchanges"] += 1  # insert into B[w]

        stack.pop()
        counters["data_exchanges"] += 1  # pop
        return found_cycle

    # Main Johnson loop
    for s in range(n):
        allowed = list(range(s, n))
        A = build_adjacency(W, allowed)

        # Reset structures for this subgraph
        for v in allowed:
            blocked[v] = False
            B[v].clear()

        # Run circuit from s
        circuit(s, s, A)

    return min_weight, min_cycles, counters

def pretty_cycle(cyc):
    # cyc is like [a,b,c,a]; format as a->b->c->a
    return "->".join(map(str, cyc))

def main():
    min_wt, cycles, counts = find_min_weight_cycles(MATRIX)
    print("Minimum cycle weight:", min_wt if min_wt is not None else "None (no cycles)")
    if cycles:
        print("Cycles with minimum weight:")
        for c in cycles:
            print("  ", pretty_cycle(c))
    print("\nInstrumentation (rough counts):")
    print("  Comparisons:", counts["comparisons"])
    print("  Data exchanges (push/pop/set updates):", counts["data_exchanges"])

if __name__ == "__main__":
    main()
