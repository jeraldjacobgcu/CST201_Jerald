
import itertools

# Hard-coded adjacency matrix (7 lectures)
# 1 = common students between lectures
# 0 = no shared students
A = [
    [0,1,1,0,1,0,0],
    [1,0,1,1,0,1,0],
    [1,1,0,1,0,1,1],
    [0,1,1,0,0,1,1],
    [1,0,0,0,0,1,1],
    [0,1,1,1,1,0,0],
    [0,0,1,1,1,0,0]
]

# ----------------------------------------------
# Helper functions
# ----------------------------------------------

def is_safe_color(v, color, colors, A):
    """Check if vertex v can take this color without conflict."""
    for u in range(len(A)):
        if A[v][u] == 1 and colors[u] == color:
            return False
    return True


def color_with_c_colors(A, c):
    """Try to color the graph using c colors (backtracking)."""
    n = len(A)
    colors = [-1] * n
    steps = 0

    def backtrack(idx):
        nonlocal steps
        if idx == n:
            return True
        for color in range(c):
            steps += 1
            if is_safe_color(idx, color, colors, A):
                colors[idx] = color
                if backtrack(idx + 1):
                    return True
                colors[idx] = -1
        return False

    ok = backtrack(0)
    return ok, colors, steps


def chromatic_number(A):
    """Find the smallest number of colors that can color the graph."""
    n = len(A)
    for c in range(1, n + 1):
        ok, colors, steps = color_with_c_colors(A, c)
        if ok:
            return c, colors, steps
    return n, list(range(n)), 0


def count_conflicts(A, assign):
    """Count number of conflicts for a given assignment."""
    n = len(A)
    conflicts = 0
    for i in range(n):
        for j in range(i + 1, n):
            if A[i][j] == 1 and assign[i] == assign[j]:
                conflicts += 1
    return conflicts


def exact_min_conflicts(A, k):
    """Find minimal conflicts using brute force for small graphs."""
    n = len(A)
    best = 10**9
    best_assign = None
    tried = 0
    for assign in itertools.product(range(k), repeat=n):
        tried += 1
        c = count_conflicts(A, assign)
        if c < best:
            best = c
            best_assign = assign
            if best == 0:
                break
    return best, best_assign, tried

# ----------------------------------------------
# Main logic
# ----------------------------------------------

print("Adjacency Matrix (Lectures):")
for row in A:
    print(row)

# 1. Minimum number of times (Chromatic Number)
cnum, coloring, checks = chromatic_number(A)
print("\n--- Part 1 ---")
print("Chromatic Number (minimum separate times):", cnum)
print("One optimal coloring (time-slot IDs start at 0):", coloring)
print("Backtracking checks:", checks)

# 2. Minimum conflicts for k=3 and k=2
print("\n--- Part 2 ---")
for k in [3, 2]:
    if k >= cnum:
        print(f"For k = {k}: No conflicts (k ≥ chromatic number).")
    else:
        best, assign, tried = exact_min_conflicts(A, k)
        print(f"For k = {k}: minimum conflicts = {best} (tried {tried} assignments)")
        print("Assignment:", assign)

print("\nProgram complete.")
