# Knapsack Problem using Greedy and Dynamic Programming approaches

def greedy_knapsack(weights, values, capacity):
    
    print("\n--- Greedy Algorithm ---")
    
    n = len(weights)
    
    ratio = [(values[i] / weights[i], weights[i], values[i]) for i in range(n)]
    
    ratio.sort(reverse=True)  # Sort by value/weight ratio (descending)

    total_value = 0
    
    total_weight = 0
    
    steps = 0

    for r, w, v in ratio:
    
        steps += 1
        
        if total_weight + w <= capacity:
        
            total_weight += w
            
            total_value += v
            
            print(f"Took item (w={w}, v={v})")
            
        else:
            
            print(f"Skipped item (w={w}, v={v})")

    print(f"Total value (Greedy): {total_value}")
    
    print(f"Total weight used: {total_weight}")
    
    print(f"Steps counted: {steps}")
    
    return total_value


def dp_knapsack(weights, values, capacity):
    
    print("\n--- Dynamic Programming (0/1 Knapsack) ---")
    
    n = len(weights)
    
    dp = [[0] * (capacity + 1) for _ in range(n + 1)]
    
    steps = 0

    for i in range(1, n + 1):
    
        for w in range(1, capacity + 1):
        
            steps += 1
            
            if weights[i - 1] > w:
            
                dp[i][w] = dp[i - 1][w]
           
            else:
            
                dp[i][w] = max(dp[i - 1][w],
                               values[i - 1] + dp[i - 1][w - weights[i - 1]])

    print(f"Maximum value (DP): {dp[n][capacity]}")
    
    print(f"Steps counted: {steps}")
    
    return dp[n][capacity]


def bounded_knapsack(weights, values, quantities, capacity):
    
    print("\n--- Bounded Knapsack (Multiple Copies) ---")
    
    expanded_weights = []
    
    expanded_values = []

    # Expand items based on their quantities
    for i in range(len(weights)):
    
        for _ in range(quantities[i]):
        
            expanded_weights.append(weights[i])
            
            expanded_values.append(values[i])

    return dp_knapsack(expanded_weights, expanded_values, capacity)


# --------------------------
# Main
# --------------------------
if __name__ == "__main__":
    
    weights = [20, 30, 40, 60, 70, 90]
    
    values = [70, 80, 90, 110, 120, 200]
    
    capacity = 280

    # Case 1: 0/1 Knapsack
    greedy_value = greedy_knapsack(weights, values, capacity)
    
    dp_value = dp_knapsack(weights, values, capacity)

    print("\nGreedy Value:", greedy_value)
    
    print("DP Value:", dp_value)

    # Case 2: Multiple copies
    quantities = [1, 2, 1, 3, 1, 2]
    
    bounded_value = bounded_knapsack(weights, values, quantities, capacity)
    
    print("Bounded Knapsack Max Value:", bounded_value)
