import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Efficient {
    // constants
    private static int GAP_PENALTY = 30;
    // A C G T
    private static int[][] MISMATCH_PENALTY = { { 0, 110, 48, 94 }, 
                                                { 110, 0, 118, 48 }, 
                                                { 48, 118, 0, 110 },
                                                { 94, 48, 110, 0 } };
    private static String SEQUENCE_INDEX = "ACGT";

    // instance variables
    private String s1; // sequence 1
    private String s2; // sequence 2
    private String a1; // alignment 1
    private String a2; // alignment 2
    private int m; // cost
    private int[][] opt;

    public static void main(String[] args) {
        String inputPath = args[0];
        String outputPath = args[1];
        Efficient efficient = new Efficient(inputPath);

        // From instructions
        double beforeUsedMem = getMemoryInKB();
        double startTime = getTimeInMilliseconds();

        efficient.setOPT();
        efficient.findAlignments();

        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage =  afterUsedMem-beforeUsedMem;
        double totalTime =  endTime - startTime;

        efficient.writeOutput(outputPath, (float) totalTime, (float) totalUsage);

        //System.out.println("CSCI 570 Project " + basic.getSequence1() + " " + basic.getSequence2() + " " + totalUsage);
    }

    /**
     * Efficient class default constructor
     */
    public Efficient() {
        s1 = "";
        s2 = "";
        a1 = "";
        a2 = "";
        m = 0;
        opt = new int[s1.length()+1][s2.length()+1];
    }

    /**
     * Efficient class constructor
     * 
     * @param input the input file
     */
    public Efficient(String input) {
        s1 = "";
        s2 = "";
        a1 = "";
        a2 = "";
        m = 0;
        generateSequences(input);
        opt = new int[s1.length()+1][s2.length()+1];
    }

    public void setOPT() {
        // initialize row 0
        for (int i1 = 0; i1 < opt[0].length; i1++) {
            opt[0][i1] = i1 * GAP_PENALTY;
        }

        // initialize col 0
        for (int j1 = 0; j1 < opt.length; j1++) {
            opt[j1][0] = j1 * GAP_PENALTY;
        }

        // recurrence
        for (int i = 1; i < opt.length; i++) {
            for (int j = 1; j < opt[0].length; j++) {
                opt[i][j] = Math.min(
                        Math.min(MISMATCH_PENALTY[SEQUENCE_INDEX.indexOf(s1.charAt(i-1))][SEQUENCE_INDEX
                                .indexOf(s2.charAt(j-1))] + opt[i - 1][j - 1], GAP_PENALTY + opt[i - 1][j]),
                        GAP_PENALTY + opt[i][j - 1]);
                //System.out.println(i + " " + j + " " + opt[i][j]);
            }
        }

        m = opt[s1.length()][s2.length()];
    }

    public void findAlignments()
    {
        // top down pass
        int i = s1.length();
        int j = s2.length();

        // fixed only this part using https://www.geeksforgeeks.org/sequence-alignment-problem/
        while (i >= 1 && j >= 1)
        {
            // x_m go horizontal
            if (opt[i-1][j] <= opt[i-1][j-1] && opt[i-1][j] <= opt[i][j-1])
            {
                a1 = s1.charAt(i-1) + a1;
                a2 = "_" + a2;
                i--;
            }
            // y_n go vertical
            else if (opt[i][j-1] <= opt[i-1][j-1] && opt[i][j-1] <= opt[i-1][j])
            {
                //a1 = s1.charAt(i) + a1;
                a2 = s2.charAt(j-1) + a2;
                a1 = "_" + a1;
                j--;
            }
            // go diagonal
            else
            {
                a1 = s1.charAt(i-1) + a1;
                a2 = s2.charAt(j-1) + a2;
                i--;
                j--;
            }
        }

        // go down column
        if (i == 0 && j > 0)
        {
            while (j > 0)
            {
                a2 = s2.charAt(j-1) + a2;
                a1 = "_" + a1;
                j--;
            }
        }

        // go horizontally
        else if (j == 0 && i > 0)
        {
            while (i > 0)
            {
                a1 = s1.charAt(i-1) + a1;
                a2 = "_" + a2;
                i--;
            }
        }
    }

    /**
     * Returns sequence1 string
     * 
     * @return sequence1 the first DNA sequence
     */
    public String getSequence1() {
        return s1;
    }

    /**
     * Returns sequence2 string
     * 
     * @return sequence2 the second DNA sequence
     */
    public String getSequence2() {
        return s2;
    }

    /**
     * Returns cost
     * 
     * @return sum of gap and mismatch costs
     */
    public int getCost() {
        return m;
    }

    /**
     * From instructions
     * Gets the memory
     * 
     * @return memory in kb
     */
    private static double getMemoryInKB() {
        double total = Runtime.getRuntime().totalMemory();
        return (total - Runtime.getRuntime().freeMemory()) / 10e3;
    }

    /**
     * From instructions
     * Gets the run time
     * 
     * @return run time in milliseconds
     */
    private static double getTimeInMilliseconds() {
        return System.nanoTime() / 10e6;
    }

    /**
     * Writes the output file
     * https://www.w3schools.com/java/java_files_create.asp
     * @param output the output file path
     */
    private void writeOutput(String output, float time, float memory)
    {
        try 
        {
            File myObj = new File(output);
            if (myObj.createNewFile()) 
            {
              System.out.println("File created: " + myObj.getName());
            } 
            else
            {
              System.out.println("File already exists.");
            }

            FileWriter myWriter = new FileWriter(output);
            myWriter.write(m + "\n");
            myWriter.write(a1 + "\n");
            myWriter.write(a2 + "\n");
            myWriter.write(Float.toString(time) + "\n");
            myWriter.write(Float.toString(memory));

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } 
        catch (IOException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Generates the sequences from the input file
     * 
     * @param input the file path to the input file
     */
    public void generateSequences(String input) {
        File myFile = new File(input);
        Scanner myScanner;

        try {
            myScanner = new Scanner(myFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return;
        }

        int j = 0;
        s1 = myScanner.nextLine();
        String tempS1 = s1;

        while (myScanner.hasNextInt()) {
            int index = myScanner.nextInt();
            j = j + 1;
            s1 = s1.substring(0, index + 1) + s1 + s1.substring(index + 1);
        }

        int k = 0;
        s2 = myScanner.next();
        String tempS2 = s2;

        while (myScanner.hasNextInt()) {
            int index = myScanner.nextInt();
            k = k + 1;
            s2 = s2.substring(0, index + 1) + s2 + s2.substring(index + 1);
        }

        myScanner.close();
        // check that lengths of s1 and s2 are 2^j*len(s1) and 2^k*len(s2)
        assert (s1.length() == Math.pow(2, j) * tempS1.length() && s2.length() == Math.pow(2, k) * tempS2.length());
    }

}