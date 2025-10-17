# CLC Battleship — Parts 1, 2, and 3 (full program)
# Values: fairness (same rules), respect (clear prompts), perseverance (try again kindly)

import random

# --- CONSTANTS ---
BOARD_SIZE   = 10
SHIP_SYMBOL  = "S"
EMPTY_SYMBOL = "."
HIT_SYMBOL   = "X"
MISS_SYMBOL  = "O"

PLAYER   = "Player"
COMPUTER = "Computer"

# Three ships total:
#  - Destroyer (2x2 square) -> 4 cells
#  - Submarine (3 diagonal) -> 3 cells (we’ll use left->right variant)
#  - Cruiser   (3 straight) -> 3 cells (we’ll use horizontal variant)
TOTAL_SHIP_CELLS = 4 + 3 + 3  # = 10

# Define the ships with their shapes (relative coordinates)
SHIPS = {
    "Destroyer":   [(0, 0), (0, 1), (1, 0), (1, 1)],   # 2x2 square
    "Submarine_LR":[(0, 0), (1, 1), (2, 2)],          # diagonal left->right
    "Submarine_RL":[(0, 2), (1, 1), (2, 0)],          # diagonal right->left (not used below)
    "Cruiser_H":   [(0, 0), (0, 1), (0, 2)],          # horizontal 3 cells
    "Cruiser_V":   [(0, 0), (1, 0), (2, 0)]           # vertical 3 cells (not used below)
}

# ------------- PART 1: BOARD CREATION + SHIP PLACEMENT -------------

def create_board():
    """Create an empty BOARD_SIZE x BOARD_SIZE board."""
    return [[EMPTY_SYMBOL for _ in range(BOARD_SIZE)] for _ in range(BOARD_SIZE)]

def display_board(board, owner=PLAYER):
    """Pretty-print a board."""
    print(f"\n{owner}'s Board:")
    print("   " + " ".join(str(i) for i in range(BOARD_SIZE)))
    for i, row in enumerate(board):
        print(f"{i:2} " + " ".join(row))
    print()

def in_bounds(r, c):
    return 0 <= r < BOARD_SIZE and 0 <= c < BOARD_SIZE

def can_place(board, shape, start_row, start_col):
    """Validate a ship shape can be placed at start_row,start_col with no overlap and within bounds."""
    for dr, dc in shape:
        r, c = start_row + dr, start_col + dc
        if not in_bounds(r, c):
            return False
        if board[r][c] != EMPTY_SYMBOL:
            return False
    return True

def place_ship(board, shape, start_row, start_col):
    """Write SHIP_SYMBOL onto the board for each cell of the shape."""
    for dr, dc in shape:
        board[start_row + dr][start_col + dc] = SHIP_SYMBOL

def place_computer_ships(board):
    """
    Randomly place the computer's three ships using brute-force trial:
    Destroyer (2x2), Submarine_LR, Cruiser_H
    """
    for ship_name in ["Destroyer", "Submarine_LR", "Cruiser_H"]:
        shape = SHIPS[ship_name]
        placed = False
        # Try random anchors until we find a valid placement
        while not placed:
            # We use 0..BOARD_SIZE-3 for simplicity; can_place() rejects out-of-bounds anyway.
            row = random.randint(0, BOARD_SIZE - 3)
            col = random.randint(0, BOARD_SIZE - 3)
            if can_place(board, shape, row, col):
                place_ship(board, shape, row, col)
                placed = True

def place_player_ships(board):
    """
    Interactive placement for the player's three ships.
    Uses the same three shapes as the computer for fairness.
    """
    print("Place your ships on the board (fairness: same 3 ships for both sides).")
    for ship_name in ["Destroyer", "Submarine_LR", "Cruiser_H"]:
        shape = SHIPS[ship_name]
        placed = False
        while not placed:
            try:
                print(f"\nPlacing your {ship_name}:")
                row = int(input("Enter starting row (0-9): "))
                col = int(input("Enter starting column (0-9): "))
                if can_place(board, shape, row, col):
                    place_ship(board, shape, row, col)
                    display_board(board)
                    placed = True
                else:
                    print("Invalid position — overlaps or out of bounds. Please try again (perseverance).")
            except ValueError:
                print("Please enter valid integers for row and column (respect).")

def setup_game():
    """Create boards and place ships for both sides."""
    player_board   = create_board()
    computer_board = create_board()

    print("\n--- Battleship Game Setup ---")
    place_player_ships(player_board)
    place_computer_ships(computer_board)

    print("\n✅ All ships placed successfully!")
    display_board(player_board)
    # Keep computer board hidden for gameplay fairness:
    # display_board(computer_board, owner=COMPUTER)

    return player_board, computer_board

# ------------------- PART 2: TURN LOGIC & TRACKING -------------------

def create_tracking_board():
    """Creates a separate board to track shots (hits/misses) against the opponent."""
    return [[EMPTY_SYMBOL for _ in range(BOARD_SIZE)] for _ in range(BOARD_SIZE)]

def player_turn(computer_board, player_tracking):
    """
    Player shoots at the computer's board.
    Returns True if hit (player goes again), False if miss (computer's turn).
    """
    while True:
        try:
            print(f"\n{PLAYER}'s Tracking Board (. = unknown, O = miss, X = hit):")
            display_board(player_tracking, owner="Your Shots")

            row = int(input("Enter target row (0-9): "))
            col = int(input("Enter target column (0-9): "))

            # Validate bounds
            if not in_bounds(row, col):
                print("Invalid position - out of bounds! Try again (respect).")
                continue

            # Check if already shot
            if player_tracking[row][col] != EMPTY_SYMBOL:
                print("You already shot this cell! Choose another (perseverance).")
                continue

            # Check hit or miss
            if computer_board[row][col] == SHIP_SYMBOL:
                print(f"HIT at ({row}, {col})!")
                player_tracking[row][col] = HIT_SYMBOL
                return True  # Player goes again
            else:
                print(f"MISS at ({row}, {col}).")
                player_tracking[row][col] = MISS_SYMBOL
                return False  # Computer's turn

        except ValueError:
            print("Please enter valid integers.")

def computer_turn(player_board, computer_tracking):
    """
    Computer shoots randomly at player's board.
    Returns True if hit (computer goes again), False if miss (player's turn).
    """
    # Brute-force: collect all untried cells
    available_cells = [(r, c)
                       for r in range(BOARD_SIZE)
                       for c in range(BOARD_SIZE)
                       if computer_tracking[r][c] == EMPTY_SYMBOL]

    if not available_cells:
        return False  # No cells left (shouldn't happen)

    # Random selection (simple and fair)
    target_row, target_col = random.choice(available_cells)
    print(f"\nComputer shoots at ({target_row}, {target_col})...")

    if player_board[target_row][target_col] == SHIP_SYMBOL:
        print(f"Computer HIT at ({target_row}, {target_col})!")
        computer_tracking[target_row][target_col] = HIT_SYMBOL
        return True
    else:
        print(f"Computer MISSED at ({target_row}, {target_col}).")
        computer_tracking[target_row][target_col] = MISS_SYMBOL
        return False

# ---------------- PART 3: WIN CHECKS & END-OF-GAME FLOW ----------------

def remaining_ship_cells(board, tracking):
    """
    Count how many ship cells on 'board' have NOT been hit yet,
    i.e., locations with SHIP_SYMBOL where 'tracking' is NOT HIT_SYMBOL.
    """
    remaining = 0
    for r in range(BOARD_SIZE):
        for c in range(BOARD_SIZE):
            if board[r][c] == SHIP_SYMBOL and tracking[r][c] != HIT_SYMBOL:
                remaining += 1
    return remaining

def show_score(player_board, computer_board, player_tracking, computer_tracking):
    """Print a small scoreboard: remaining ship cells for each side."""
    p_rem = remaining_ship_cells(player_board, computer_tracking)
    c_rem = remaining_ship_cells(computer_board, player_tracking)
    print(f"\n[Scoreboard] {PLAYER} ship-cells left: {p_rem} | {COMPUTER} ship-cells left: {c_rem}")

def play_game(player_board, computer_board):
    """
    Main game loop with win conditions.
    A hit grants another shot; we check for victory immediately after each shot.
    """
    player_tracking   = create_tracking_board()  # your shots on computer
    computer_tracking = create_tracking_board()  # computer shots on you

    print("\n=== GAME START ===")
    print("Player goes first!")

    current_player = PLAYER

    while True:
        show_score(player_board, computer_board, player_tracking, computer_tracking)

        if current_player == PLAYER:
            print("\n--- YOUR TURN ---")
            hit = player_turn(computer_board, player_tracking)

            # Win check right after the shot
            comp_remaining = remaining_ship_cells(computer_board, player_tracking)
            if comp_remaining == 0:
                print("\n You sank all enemy ships. YOU WIN! Thanks for playing with fairness, respect, and perseverance.")
                # (Optional) Reveal computer board for closure:
                # display_board(computer_board, owner=COMPUTER)
                break

            if hit:
                print("You hit! Go again.")
                # Stay on player's turn
            else:
                print("You missed. Computer's turn.")
                current_player = COMPUTER

        else:
            print("\n--- COMPUTER'S TURN ---")
            hit = computer_turn(player_board, computer_tracking)

            # Win check right after the shot
            player_remaining = remaining_ship_cells(player_board, computer_tracking)
            if player_remaining == 0:
                print("\n Computer sank all your ships. COMPUTER WINS!")
                print("Thank you for the respectful match—keep persevering and try a new strategy next time!")
                # (Optional) Reveal computer's shots:
                # display_board(computer_tracking, owner="Computer Shots")
                break

            if hit:
                print("Computer hit! Going again...")
                # Stay on computer's turn
            else:
                print("Computer missed. Your turn.")
                current_player = PLAYER

# ------------------------ MAIN DRIVER ------------------------

def main():
    player_board, computer_board = setup_game()
    play_game(player_board, computer_board)

if __name__ == "__main__":
    main()
