import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Arrays;

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

    public static void main(String[] args) {
        String inputPath = args[0];
        String outputPath = args[1];
        Efficient efficient = new Efficient(inputPath);

        // From instructions
        double beforeUsedMem = getMemoryInKB();
        double startTime = getTimeInMilliseconds();

        int cost = efficient.runEfficient();

        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage =  afterUsedMem-beforeUsedMem;
        double totalTime =  endTime - startTime;

        efficient.writeOutput(outputPath, cost, (float) totalTime, (float) totalUsage);

        System.out.println("Efficient " + efficient.getSequence1() + " " + efficient.getSequence2() + " " + totalUsage);
    }

    /**
     * Efficient class default constructor
     */
    public Efficient() {
        s1 = "";
        s2 = "";
        a1 = "";
        a2 = "";
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
        generateSequences(input);
    }

    /**
     * 
     * @param x xL string until xMid (forwards) or xR string until xMid (backwards)
     * @param y full y string (forwards for yL, backwards for yR)
     */
    public int[] setCost(String x, String y, int[] startCol)
    {
        // xL by Y, left column
        int[] col1= new int[y.length() + 1];
        // xL by Y, right column
        int[] col2 = new int[y.length() + 1];
        int index = 1;

        while (index <= x.length())
        {
            // beginning column for x
            if (index == 1 && startCol == null)
            {
                for (int i1 = 0; i1 < col1.length; i1++)
                {
                    col1[i1] = i1 * GAP_PENALTY;
                }
            }
            else if (index == 1)
            {
                col1 = startCol;
            }

            // first value of second column
            col2[0] = index * GAP_PENALTY;

            // rest of values of second column
            for (int j = 1; j < col2.length; j++)
            {
                col2[j] = Math.min(
                        Math.min(MISMATCH_PENALTY[SEQUENCE_INDEX.indexOf(x.charAt(index-1))][SEQUENCE_INDEX
                                .indexOf(y.charAt(j-1))] + col1[j - 1], GAP_PENALTY + col1[j]),
                        GAP_PENALTY + col2[j - 1]);
            }

            index++; 
            
            // move col2 to col1 for next iteration
            for (int k = 0; k < col1.length; k++)
            {
                col1[k] = col2[k];
            }
        }

        return col2;
    }

    public int divide2(String xL, String xR, String y)
    {
        int cost = 0;

        int[] colL = setCost(xL, y, null); // end column of xL
        int[] colR = setCost(xR, y, colL); // end column of xR
        int min = colL[0] + colR[colR.length - 1];
        int yMid = 0;

        for (int i = 1; i < colL.length; i++)
        {
            if (colL[i] + colR[colR.length-1-i] < min)
            {
                min = colL[i] + colR[colR.length-1-i];
                yMid = i;
            }
        }

        cost = cost + min;
        System.out.println(Arrays.toString(colL));
        System.out.println(Arrays.toString(colR));
        System.out.println("Cost first: " + cost);

        // base cases
        if (xL.length() <= 2)
        {
            //a1 = a1 + xL;
            System.out.println("xL here");
            System.out.println("Cost: " + cost);
            return cost;
        }
        if (xR.length() <= 2)
        {
            //a1 = xR + a1;
            System.out.println("xR here");
            System.out.println("Cost: " + cost);
            return cost;
        }
        if (y.length() <= 2)
        {
            //a2 = a2 + y;
            System.out.println("Y here");
            System.out.println("Cost: " + cost);
            return cost;
        }
        else
        {
            String yL = "";
            String yR = "";

            System.out.print("Y length and yMid " + y.length() +  " " + yMid);

            yL = y.substring(0, yMid-1);
            yR = reverseSubstring(y, yMid-1);

            // divide the left side
            cost = cost + divide2(xL.substring(0, xL.length()/2), reverseSubstring(xL, xL.length()/2), yL);
            System.out.println("Cost divide L: " + cost);
            // divide the right side
            cost = cost + divide2(xR.substring(0, xR.length()/2), reverseSubstring(xR, xR.length()/2), yR);
            System.out.println("Cost divide R: " + cost);
        }

        System.out.println("Cost: " + cost);
        return cost;
    }

    /** 
    public void findAlignments(String x, String y)
    {
        int[][] opt = new int [x.length()][y.length()];

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
        // top down pass
        int i = x.length();
        int j = y.length();

        // fixed only this part using https://www.geeksforgeeks.org/sequence-alignment-problem/
        while (i >= 1 && j >= 1)
        {
            // x_m go horizontal
            if (opt[i-1][j] <= opt[i-1][j-1] && opt[i-1][j] <= opt[i][j-1])
            {
                a1 = x.charAt(i-1) + a1;
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
    */

    public String reverseSubstring(String str, int index)
    {
        String rev = "";

        for (int i = str.length()-1; i >= index; i--)
        {
            rev = rev + str.charAt(i);
        }
        System.out.println("rev " + rev);
        return rev;
        
    }

    public int runEfficient()
    {
        // beginning case
        // floor of x/2
        int xMid = s1.length()/2;
        // substring is O(n) operation
        String xL = s1.substring(0, xMid);
        String xR = s1.substring(xMid);
        String y = s2;
        return divide2(xL, xR, y);
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
    private void writeOutput(String output, int cost, float time, float memory)
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
            myWriter.write(cost + "\n");
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