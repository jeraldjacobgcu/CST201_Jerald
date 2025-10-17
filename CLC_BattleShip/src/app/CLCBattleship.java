package app;
import java.util.*;
import java.util.stream.Collectors;

public class CLCBattleship {

    // --- Game constants ---
    static final int N = 10;
    static final char EMPTY = '.';
    static final char HIT = 'X';
    static final char MISS = 'o';

    static final char DESTROYER = 'D'; // 2x2 square
    static final char SUBMARINE = 'S'; // 3 diagonal ("/" or "\")
    static final char CRUISER   = 'C'; // 3 in a line (H/V)

    static class Coord {
        final int r, c;
        Coord(int r, int c) { this.r = r; this.c = c; }
    }

    static class Ship {
        final String name;
        final char mark;
        final List<Coord> cells;
        Ship(String name, char mark, List<Coord> cells) {
            this.name = name;
            this.mark = mark;
            this.cells = cells;
        }
    }

    static class Board {
        char[][] ships = new char[N][N];
        char[][] shots = new char[N][N];
        boolean[][] tried = new boolean[N][N];
        List<Ship> shipList = new ArrayList<>();

        Board() {
            for (int r = 0; r < N; r++) {
                Arrays.fill(ships[r], EMPTY);
                Arrays.fill(shots[r], EMPTY);
            }
        }

        boolean inBounds(int r, int c) {
            return r >= 0 && r < N && c >= 0 && c < N;
        }

        boolean canPlace(List<Coord> cells) {
            for (Coord k : cells) {
                if (!inBounds(k.r, k.c)) return false;
                if (ships[k.r][k.c] != EMPTY) return false;
            }
            return true;
        }

        void place(String name, char mark, List<Coord> cells) {
            Ship s = new Ship(name, mark, cells);
            for (Coord k : cells) ships[k.r][k.c] = mark;
            shipList.add(s);
        }

        String fireAt(int r, int c) {
            if (!inBounds(r, c)) return "INVALID";
            if (tried[r][c]) return "REPEAT";
            tried[r][c] = true;
            if (ships[r][c] != EMPTY) {
                shots[r][c] = HIT;
                char m = ships[r][c];
                if (shipNowSunk(m)) return "HIT&SUNK:" + m;
                return "HIT";
            } else {
                shots[r][c] = MISS;
                return "MISS";
            }
        }

        boolean shipNowSunk(char mark) {
            for (Ship s : shipList) {
                if (s.mark == mark) {
                    for (Coord k : s.cells) {
                        if (shots[k.r][k.c] != HIT) return false;
                    }
                    return true;
                }
            }
            return false;
        }

        boolean allSunk() {
            for (Ship s : shipList) {
                for (Coord k : s.cells) {
                    if (shots[k.r][k.c] != HIT) return false;
                }
            }
            return true;
        }

        void printShots(String title) {
            System.out.println(title);
            System.out.print("   ");
            for (int c = 0; c < N; c++) System.out.print((c+1 < 10 ? " "+(c+1) : (c+1)) + " ");
            System.out.println();
            for (int r = 0; r < N; r++) {
                char rowLabel = (char)('A' + r);
                System.out.printf("%2s ", rowLabel);
                for (int c = 0; c < N; c++) {
                    System.out.print(shots[r][c] + "  ");
                }
                System.out.println();
            }
        }

        void printOwn(String title) { // shows player’s own board
            System.out.println(title);
            System.out.print("   ");
            for (int c = 0; c < N; c++) System.out.print((c+1 < 10 ? " "+(c+1) : (c+1)) + " ");
            System.out.println();
            for (int r = 0; r < N; r++) {
                char rowLabel = (char)('A' + r);
                System.out.printf("%2s ", rowLabel);
                for (int c = 0; c < N; c++) {
                    System.out.print(ships[r][c] + "  ");
                }
                System.out.println();
            }
        }
    }

    // --- Brute-force pattern generators for placements ---
    static List<List<Coord>> placementsDestroyer(Board b) {
        List<List<Coord>> out = new ArrayList<>();
        for (int r = 0; r <= N-2; r++) {
            for (int c = 0; c <= N-2; c++) {
                List<Coord> cells = Arrays.asList(
                        new Coord(r,c), new Coord(r,c+1),
                        new Coord(r+1,c), new Coord(r+1,c+1));
                if (b.canPlace(cells)) out.add(cells);
            }
        }
        return out;
    }

    static List<List<Coord>> placementsCruiser(Board b, boolean horizontal) {
        List<List<Coord>> out = new ArrayList<>();
        if (horizontal) {
            for (int r = 0; r < N; r++) for (int c = 1; c < N-1; c++) {
                List<Coord> cells = Arrays.asList(
                        new Coord(r,c-1), new Coord(r,c), new Coord(r,c+1));
                if (b.canPlace(cells)) out.add(cells);
            }
        } else {
            for (int r = 1; r < N-1; r++) for (int c = 0; c < N; c++) {
                List<Coord> cells = Arrays.asList(
                        new Coord(r-1,c), new Coord(r,c), new Coord(r+1,c));
                if (b.canPlace(cells)) out.add(cells);
            }
        }
        return out;
    }

    static List<List<Coord>> placementsSubmarine(Board b, boolean backslash) {
        // backslash "\" => (r,c),(r+1,c+1),(r+2,c+2)
        // slash "/"     => (r,c),(r+1,c-1),(r+2,c-2)
        List<List<Coord>> out = new ArrayList<>();
        if (backslash) {
            for (int r = 0; r <= N-3; r++) for (int c = 0; c <= N-3; c++) {
                List<Coord> cells = Arrays.asList(
                        new Coord(r,c), new Coord(r+1,c+1), new Coord(r+2,c+2));
                if (b.canPlace(cells)) out.add(cells);
            }
        } else {
            for (int r = 0; r <= N-3; r++) for (int c = 2; c < N; c++) {
                List<Coord> cells = Arrays.asList(
                        new Coord(r,c), new Coord(r+1,c-1), new Coord(r+2,c-2));
                if (b.canPlace(cells)) out.add(cells);
            }
        }
        return out;
    }

    // --- Utility parsing: accept inputs like "A5" or "a 5" or "1 3" ---
    static Coord parseCell(Scanner sc) {
        while (true) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String s = line.toUpperCase().replaceAll("[,;]+"," ");
            String[] parts = s.split("\\s+");

            try {
                if (parts.length == 1) {
                    // e.g., "A5"
                    String p = parts[0];
                    if (p.length() >= 2 && Character.isLetter(p.charAt(0))) {
                        int r = p.charAt(0) - 'A';
                        int c = Integer.parseInt(p.substring(1)) - 1;
                        return new Coord(r, c);
                    }
                }
                if (parts.length >= 2) {
                    // e.g., "A 5" or "3 7"
                    int r, c;
                    if (Character.isLetter(parts[0].charAt(0))) {
                        r = parts[0].charAt(0) - 'A';
                        c = Integer.parseInt(parts[1]) - 1;
                    } else {
                        r = Integer.parseInt(parts[0]) - 1;
                        c = Integer.parseInt(parts[1]) - 1;
                    }
                    return new Coord(r, c);
                }
            } catch (Exception ignored) {}

            System.out.print("Please enter a valid cell like A5 or 3 7: ");
        }
    }

    static void placePlayerShips(Board pb, Scanner sc) {
        System.out.println("\n— Ship placement (your board) —");
        System.out.println("We’ll practice fairness (no overlap), respect (clear prompts), and perseverance (try again if invalid).");

        // Destroyer 2x2 (top-left anchor)
        while (true) {
            System.out.print("Place DESTROYER (2x2). Enter TOP-LEFT cell (e.g., B3): ");
            Coord topLeft = parseCell(sc);
            List<Coord> cells = Arrays.asList(
                    new Coord(topLeft.r, topLeft.c),
                    new Coord(topLeft.r, topLeft.c+1),
                    new Coord(topLeft.r+1, topLeft.c),
                    new Coord(topLeft.r+1, topLeft.c+1)
            );
            if (pb.canPlace(cells)) { pb.place("Destroyer", DESTROYER, cells); break; }
            System.out.println("Invalid (out of bounds or overlap). With perseverance, try a new spot.");
        }
        pb.printOwn("Your board after placing DESTROYER:");

        // Submarine diagonal (center + direction)
        while (true) {
            System.out.print("Place SUBMARINE (diagonal 3). Enter CENTER cell (e.g., E5): ");
            Coord center = parseCell(sc);
            System.out.print("Direction (\\ for down-right, / for down-left): ");
            String dir = sc.nextLine().trim();
            boolean backslash = dir.equals("\\");
            List<Coord> cells;
            if (backslash) {
                cells = Arrays.asList(
                        new Coord(center.r-1, center.c-1),
                        new Coord(center.r, center.c),
                        new Coord(center.r+1, center.c+1)
                );
            } else {
                cells = Arrays.asList(
                        new Coord(center.r-1, center.c+1),
                        new Coord(center.r, center.c),
                        new Coord(center.r+1, center.c-1)
                );
            }
            if (pb.canPlace(cells)) { pb.place("Submarine", SUBMARINE, cells); break; }
            System.out.println("Invalid (out of bounds or overlap). Let’s respectfully try again.");
        }
        pb.printOwn("Your board after placing SUBMARINE:");

        // Cruiser 3 in a line (center + H/V)
        while (true) {
            System.out.print("Place CRUISER (3-in-line). Enter CENTER cell (e.g., H6): ");
            Coord center = parseCell(sc);
            System.out.print("Orientation (H for horizontal, V for vertical): ");
            String ori = sc.nextLine().trim().toUpperCase();
            List<Coord> cells;
            if (ori.startsWith("H")) {
                cells = Arrays.asList(
                        new Coord(center.r, center.c-1),
                        new Coord(center.r, center.c),
                        new Coord(center.r, center.c+1)
                );
            } else {
                cells = Arrays.asList(
                        new Coord(center.r-1, center.c),
                        new Coord(center.r, center.c),
                        new Coord(center.r+1, center.c)
                );
            }
            if (pb.canPlace(cells)) { pb.place("Cruiser", CRUISER, cells); break; }
            System.out.println("Invalid (out of bounds or overlap). Keep going—you’ve got this.");
        }
        pb.printOwn("Your final board after all ships:");
    }

    static void placeCPUShips(Board cb, Random rng) {
        // Use brute-force scans to build candidate placements, then pick random non-overlapping ones
        // Destroyer
        List<List<Coord>> dAll = placementsDestroyer(cb);
        Collections.shuffle(dAll, rng);
        cb.place("Destroyer", DESTROYER, dAll.get(0));

        // Submarine: choose orientation randomly
        List<List<Coord>> sAll = new ArrayList<>();
        sAll.addAll(placementsSubmarine(cb, true));
        sAll.addAll(placementsSubmarine(cb, false));
        Collections.shuffle(sAll, rng);
        for (List<Coord> s : sAll) {
            if (cb.canPlace(s)) { cb.place("Submarine", SUBMARINE, s); break; }
        }

        // Cruiser: choose orientation randomly
        List<List<Coord>> cAll = new ArrayList<>();
        cAll.addAll(placementsCruiser(cb, true));
        cAll.addAll(placementsCruiser(cb, false));
        Collections.shuffle(cAll, rng);
        for (List<Coord> s : cAll) {
            if (cb.canPlace(s)) { cb.place("Cruiser", CRUISER, s); break; }
        }
    }

    static Coord cpuRandomUntargeted(Board target, Random rng) {
        List<Coord> opts = new ArrayList<>();
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) {
            if (!target.tried[r][c]) opts.add(new Coord(r,c));
        }
        return opts.get(rng.nextInt(opts.size()));
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rng = new Random();

        System.out.println("Welcome! This CLC game practices fairness (same rules for all),");
        System.out.println("respect (clear, kind prompts), and perseverance (try again when errors happen).");
        System.out.println("Board is 10x10. Rows A–J, columns 1–10. Hits earn an extra turn.\n");

        Board player = new Board();
        Board cpu = new Board();

        placePlayerShips(player, sc);
        placeCPUShips(cpu, rng);

        System.out.println("\nLet’s begin. Take turns fairly; no repeated shots; out-of-bounds is not allowed.");
        boolean playerTurn = true;

        while (true) {
            if (playerTurn) {
                player.printShots("Your SHOTS vs Computer (X=hit, o=miss):");
                System.out.print("Your turn. Enter target cell (e.g., C7): ");
                Coord k = parseCell(sc);
                String res = cpu.fireAt(k.r, k.c);
                if ("INVALID".equals(res)) {
                    System.out.println("Out of bounds. With perseverance, please try again.");
                    continue;
                }
                if ("REPEAT".equals(res)) {
                    System.out.println("Already tried that cell. Please choose another.");
                    continue;
                }
                if (res.startsWith("HIT&SUNK")) {
                    char m = res.charAt(res.length()-1);
                    System.out.println("HIT! You SUNK the opponent’s " + shipName(m) + "! You shoot again.");
                } else if ("HIT".equals(res)) {
                    System.out.println("HIT! You shoot again.");
                } else {
                    System.out.println("Miss. Computer’s turn.");
                    playerTurn = false;
                }
                if (cpu.allSunk()) {
                    System.out.println("\nYou win! Thanks for playing with fairness, respect, and perseverance.");
                    break;
                }
            } else {
                // CPU turn
                Coord k = cpuRandomUntargeted(player, rng);
                String cellLabel = "" + (char)('A'+k.r) + (k.c+1);
                String res = player.fireAt(k.r, k.c);
                if ("HIT".equals(res) || res.startsWith("HIT&SUNK")) {
                    if (res.startsWith("HIT&SUNK")) {
                        char m = res.charAt(res.length()-1);
                        System.out.println("Computer fires at " + cellLabel + " — HIT & your " + shipName(m) + " is SUNK! Computer shoots again.");
                    } else {
                        System.out.println("Computer fires at " + cellLabel + " — HIT! Computer shoots again.");
                    }
                } else {
                    System.out.println("Computer fires at " + cellLabel + " — Miss. Your turn.");
                    playerTurn = true;
                }
                if (player.allSunk()) {
                    System.out.println("\nComputer wins. Thank you for the respectful match—great perseverance!");
                    break;
                }
            }
        }
    }

    static String shipName(char m) {
        if (m == DESTROYER) return "Destroyer";
        if (m == SUBMARINE) return "Submarine";
        if (m == CRUISER)   return "Cruiser";
        return "Ship";
    }
}
