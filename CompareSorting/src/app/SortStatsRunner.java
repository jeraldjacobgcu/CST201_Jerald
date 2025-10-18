package app;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class SortStatsRunner {

    // This class holds counts for comparisons and data exchanges (writes).
    static class Stats 
    {
        long comparisons = 0; 
        
        long exchanges = 0;   

        void add(Stats other) 
        {
            this.comparisons += other.comparisons;
            
            this.exchanges += other.exchanges;
        }
    }

   
    static int[] makeArray(int n) 
    {
        int[] a = new int[n];
        
        for (int i = 0; i < n; i++) 
        {
            a[i] = i + 1; 
        }
        return a;
    }

    // Fisher-Yates shuffle
    static void shuffle(int[] a, Random rng) 
    {
        for (int i = a.length - 1; i > 0; i--) 
        {
            int j = rng.nextInt(i + 1);
            
            int tmp = a[i];
            
            a[i] = a[j];
            
            a[j] = tmp;
        }
    }

    // swap that DOES count exchanges (3 writes) during sorting
    static void swap(int[] a, int i, int j, Stats st) 
    {
        if (i == j) return;
        
        int tmp = a[i]; st.exchanges++;
       
        a[i] = a[j];   st.exchanges++;
       
        a[j] = tmp;    st.exchanges++;
    }

    // ---------- Selection Sort ----------
    static void selectionSort(int[] a, Stats st) 
    {
        int n = a.length;
        
        for (int i = 0; i < n - 1; i++) 
        {
            int minIndex = i;
            
            for (int j = i + 1; j < n; j++) 
            {
                st.comparisons++;
                
                if (a[j] < a[minIndex]) 
                {
                    minIndex = j;
                }
            }
            swap(a, i, minIndex, st);
        }
    }

    // ---------- Bubble Sort (with early stop) ----------
    static void bubbleSort(int[] a, Stats st) 
    {
        int n = a.length;
        
        boolean swapped;
        
        for (int i = 0; i < n - 1; i++) 
        {
            swapped = false;
            
            for (int j = 0; j < n - 1 - i; j++) 
            {
                st.comparisons++;
                
                if (a[j] > a[j + 1]) 
                {
                    swap(a, j, j + 1, st);
                    
                    swapped = true;
                }
            }
            if (!swapped) 
            {
                break; 
            }
        }
    }

    // ---------- Merge Sort ----------
    static void mergeSort(int[] a, Stats st)
    {
        
        int[] temp = new int[a.length];
        
        mergeSortRec(a, 0, a.length - 1, temp, st);
    }

    static void mergeSortRec(int[] a, int left, int right, int[] temp, Stats st) 
    {
        if (left >= right) return;
        
        int mid = (left + right) / 2;
        
        mergeSortRec(a, left, mid, temp, st);
        
        mergeSortRec(a, mid + 1, right, temp, st);
        
        merge(a, left, mid, right, temp, st);
    }

    static void merge(int[] a, int left, int mid, int right, int[] temp, Stats st) 
    {
        int i = left;
        
        int j = mid + 1;
        
        int k = 0;

        // Merge into temp (each write to temp counts as an exchange)
        while (i <= mid && j <= right) 
        {
            st.comparisons++;
            
            if (a[i] <= a[j]) 
            {
                temp[k] = a[i];
                
                st.exchanges++;
                
                i++;
            } 
            else 
            {
                temp[k] = a[j];
                
                st.exchanges++;
                
                j++;
            }
            
            k++;
        }
        while (i <= mid) 
        {
            temp[k] = a[i];
            
            st.exchanges++;
            
            i++; k++;
        }
        while (j <= right) 
        {
            temp[k] = a[j];
            
            st.exchanges++;
            
            j++; k++;
        }

        // Copy back to a
        for (int t = 0; t < k; t++) 
        {
            a[left + t] = temp[t];
            
            st.exchanges++;
        }
    }

    // ---------- Quick Sort (simple: last element as pivot) ----------
    static void quickSort(int[] a, Stats st) 
    {
        quickRec(a, 0, a.length - 1, st);
    }

    static void quickRec(int[] a, int low, int high, Stats st) 
    {
        if (low >= high) return;
        
        int p = partition(a, low, high, st);
        
        quickRec(a, low, p - 1, st);
        
        quickRec(a, p + 1, high, st);
    }

    static int partition(int[] a, int low, int high, Stats st) 
    {
        int pivot = a[high]; // reading value doesn't count
        
        int i = low;
        
        for (int j = low; j < high; j++) 
        {
            st.comparisons++;
            
            if (a[j] <= pivot) 
            {
                swap(a, i, j, st);
                
                i++;
            }
        }
        
        swap(a, i, high, st);
        
        return i;
    }

    // ---------- "Better" Algorithm: Randomized Quick Sort ----------
    
    // This is like quick sort, but it picks a random pivot each time.
   
    static void randomizedQuickSort(int[] a, Stats st, Random rng)
    {
        randomizedQuickRec(a, 0, a.length - 1, st, rng);
    }

    static void randomizedQuickRec(int[] a, int low, int high, Stats st, Random rng) 
    {
        if (low >= high) return;

        // choose a random pivot index and swap to the end
        int pivotIndex = low + rng.nextInt(high - low + 1);
        
        swap(a, pivotIndex, high, st);

        int p = partition(a, low, high, st);
        
        randomizedQuickRec(a, low, p - 1, st, rng);
        
        randomizedQuickRec(a, p + 1, high, st, rng);
    }

    // ---------- Main program ----------
    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in);
        
        System.out.print("Enter n: ");
        
        int n = sc.nextInt();
        
        final int TRIALS = 100;
        
        Random rng = new Random(123); // fixed seed so results are repeatable

        Stats selTotal = new Stats();
        
        Stats bubTotal = new Stats();
        
        Stats merTotal = new Stats();
        
        Stats quiTotal = new Stats();
        
        Stats rqsTotal = new Stats(); // better algorithm

        for (int t = 1; t <= TRIALS; t++) 
        {
            // Step 1: make array 1..n
            int[] base = makeArray(n);

            // Step 2: shuffle
            shuffle(base, rng);

            // Make copies so each algorithm gets the same input
            int[] a1 = Arrays.copyOf(base, base.length);
            
            int[] a2 = Arrays.copyOf(base, base.length);
            
            int[] a3 = Arrays.copyOf(base, base.length);
            
            int[] a4 = Arrays.copyOf(base, base.length);
            
            int[] a5 = Arrays.copyOf(base, base.length);
            
            // Run each sort and collect stats
            Stats s1 = new Stats();
            
            selectionSort(a1, s1);
            
            selTotal.add(s1);

            Stats s2 = new Stats();
            
            bubbleSort(a2, s2);
            
            bubTotal.add(s2);

            Stats s3 = new Stats();
            
            mergeSort(a3, s3);
            
            merTotal.add(s3);

            Stats s4 = new Stats();
            
            quickSort(a4, s4);
            
            quiTotal.add(s4);

            Stats s5 = new Stats();
            
            randomizedQuickSort(a5, s5, rng); // better algorithm
            
            rqsTotal.add(s5);
        }

        // Print averages
        System.out.println();
        
        System.out.println("Averages over " + TRIALS + " trials (n = " + n + ")");
        
        System.out.println("Algorithm                 Avg Comparisons          Avg Data Exchanges");
        
        System.out.println("---------------------------------------------------------------------");
        
        System.out.printf("Selection Sort         %20.2f %24.2f%n",
                selTotal.comparisons / (double) TRIALS, selTotal.exchanges / (double) TRIALS);
        
        System.out.printf("Bubble Sort            %20.2f %24.2f%n",
                bubTotal.comparisons / (double) TRIALS, bubTotal.exchanges / (double) TRIALS);
        
        System.out.printf("Merge Sort             %20.2f %24.2f%n",
                merTotal.comparisons / (double) TRIALS, merTotal.exchanges / (double) TRIALS);
        
        System.out.printf("Quick Sort             %20.2f %24.2f%n",
                quiTotal.comparisons / (double) TRIALS, quiTotal.exchanges / (double) TRIALS);
        
        System.out.printf("Randomized Quick Sort  %20.2f %24.2f%n",
                rqsTotal.comparisons / (double) TRIALS, rqsTotal.exchanges / (double) TRIALS);

        System.out.println();
        
        System.out.println("Notes:");
        
        System.out.println("- A comparison is any element-to-element check (like <= or <).");
        
        System.out.println("- A data exchange is any write into the array. A swap uses 3 writes.");
        
        System.out.println("- The 'better' algorithm uses a random pivot to avoid bad splits.");
    }
}
