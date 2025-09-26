package app;

import java.util.Scanner;

//Test

public class AnagramChecker
{
    public static boolean isAnagram(String a, String b) 
    {
        if (a == null || b == null) return false;
        
        if (a.length() != b.length()) return false; // fast size check

        // Normalize to ignore case; remove this if case-sensitive is required.
        a = a.toLowerCase();
        
        b = b.toLowerCase();

        // Count characters using UTF-16 code units (safe and fast).
        int[] counts = new int[65536];
        
        for (int i = 0; i < a.length(); i++) 
        {
            counts[a.charAt(i)]++;
            
            counts[b.charAt(i)]--;
        }
        
        for (int c : counts) 
        {
            if (c != 0) return false;
        }
        return true;
    }

    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in);
        
        // Read two words (tokens). Example input: tea eat
        
        System.out.println("Enter First Word");
        
        String w1 = sc.next();

        System.out.println("Enter Second Word");
        
        String w2 = sc.next();
        
        sc.close();
        
        boolean anagramstate =  isAnagram(w1, w2);

        if (anagramstate)
        {
        	System.out.println("\nBoth words are Anagrams");
        }
        else
        {
        	System.out.println("\nBoth words are NOT Anagrams");
        }
    }
}
