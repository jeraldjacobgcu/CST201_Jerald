package app;

import java.math.BigInteger;

import java.util.Scanner;

public class towerOfHanoi {
    // Returns 2^(n - i) using iterative doubling (nonrecursive)
    static BigInteger movesOfDisk(int n, int i) {
        if (i < 1 || i > n) throw new IllegalArgumentException("i must satisfy 1 ≤ i ≤ n");
        int k = n - i;
        BigInteger moves = BigInteger.ONE;
        for (int t = 0; t < k; t++) {
            moves = moves.shiftLeft(1); // multiply by 2 without recursion
        }
        return moves;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter n (total disks): ");
        int n = sc.nextInt();
        System.out.print("Enter i (1 = smallest ... n = largest): ");
        int i = sc.nextInt();

        try {
            BigInteger moves = movesOfDisk(n, i);
            System.out.println("Moves made by disk " + i + " (of " + n + "): " + moves);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
