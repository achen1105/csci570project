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

    public static void main(String[] args) {
        String inputPath = args[0];
        String outputPath = args[1];
        Efficient efficient = new Efficient(inputPath);

        System.gc();

        // From instructions
        double beforeUsedMem = getMemoryInKB();
        double startTime = getTimeInMilliseconds();

        efficient.runEfficient();

        int cost = efficient.getCost();

        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage =  afterUsedMem-beforeUsedMem;
        double totalTime =  endTime - startTime;

        efficient.writeOutput(outputPath, cost, (float) totalTime, (float) totalUsage);

        //System.out.println("Efficient " + efficient.getSequence1() + " " + efficient.getSequence2() + " " + totalUsage);
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
    public int[] setCost(String x, String y)
    {
        // xL by Y, left column
        int[] col1= new int[y.length() + 1];
        // xL by Y, right column
        int[] col2 = new int[y.length() + 1];
        int index = 1;

        while (index <= x.length())
        {
            // beginning column for x
            if (index == 1)
            {
                for (int i1 = 0; i1 < col1.length; i1++)
                {
                    col1[i1] = i1 * GAP_PENALTY;
                }
            }

            // first value of second column
            col2[0] = index * GAP_PENALTY;

            // rest of values of second column
            for (int j = 1; j < col2.length; j++)
            {
                // match case
                if (x.charAt(index-1) == y.charAt(j-1))
                {
                    col2[j] = col1[j-1];
                }
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

    public String[] divide(String x, String y)
    {
        //int cost = 0;
        String al1 = "";
        String al2 = "";

        // base cases
        if (x.length() <= 2)
        { 
            //System.out.println(x + " " + y);
            // System.out.println("Cost: " + cost);
            //String[] temp = getAlignments(x, y);
            //System.out.println("end of divide base case 1" + Arrays.toString(temp));
            //return temp;
            //return cost;
            return getAlignments(x, y);
        }
        else if (y.length() <= 2)
        {
            //System.out.println(x + " " + y);
            // System.out.println("Cost: " + cost);
            //String[] temp = getAlignments(x, y);
            //System.out.println("end of divide base case 2" + Arrays.toString(temp));
            //return temp;
            //return cost;
            return getAlignments(x, y);
        }
        else
        {
            int[] colL = setCost(x.substring(0, x.length()/2), y); // end column of xL
            int[] colR = setCost(reverseSubstring(x, x.length()/2), reverseSubstring(y, 0)); // end column of xR
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

            //cost = cost + min;
            //System.out.println(Arrays.toString(colL));
            //System.out.println(Arrays.toString(colR));
            //System.out.println("Cost first: " + cost);
            String yL = "";
            String yR = y;

            //System.out.println("Y length and yMid " + y.length() +  " " + yMid);

            if (yMid >= 1)
            {
                yL = y.substring(0, yMid);
                yR = y.substring(yMid);
            }

            // divide the lower left side
            String[] tempLower = divide(x.substring(0, x.length()/2), yL);
            String[] tempUpper = divide(x.substring(x.length()/2), yR);
            // divide the upper right side
            // first alignment
            al1 =  tempLower[0] + tempUpper[0];
            //System.out.println("ALIGNMENT 1: " + al1);
            
            // second alignment
            al2 = tempLower[1] + tempUpper[1];
            //System.out.println("ALIGNMENT 2: " + al2);
        }

        //System.out.println("Cost: " + cost);
        //String[] temp = new String[]{al1, al2};
        //System.out.println("end of divide method, alignments: " + Arrays.toString(temp));
        //return temp;
        System.gc();
        return new String[]{al1, al2};
    }

    public String reverseSubstring(String str, int index)
    {
        String rev = "";
        
        for (int i = str.length()-1; i >= index; i--)
        {
            rev = rev + str.charAt(i);
        }
        //System.out.println("rev " + rev);
        return rev;
        
    }

    public int getCost()
    {
        int cost = 0;

        for (int i = 0; i < a1.length(); i++)
        {
            if (i < a2.length())
            {
                // alignment
                if (a1.charAt(i)==a2.charAt(i))
                {
                    cost = cost + MISMATCH_PENALTY[0][0];
                }
                // gap
                else if (a1.charAt(i) == '_' || a2.charAt(i) == '_')
                {
                    cost = cost + GAP_PENALTY;
                }
                // mismatch
                else
                {
                    cost = cost + MISMATCH_PENALTY[SEQUENCE_INDEX.indexOf(a1.charAt(i))][SEQUENCE_INDEX.indexOf(a2.charAt(i))];
                }
            }
        }

        return cost;
    }

    // basic version for base cases
    public String[] getAlignments(String x, String y)
    {
        //System.out.println("START ALIGNMENT " + x + " " + y);
        String align1 = "";
        String align2 = "";
        int[][] opt = new int[x.length()+1][y.length()+1];
        // initialize row 0
        for (int i1 = 0; i1 < opt[0].length; i1++) {
            opt[0][i1] = i1 * GAP_PENALTY;
            //System.out.println(0 + " " + i1 + " " + opt[0][i1]);
        }

        // initialize col 0
        for (int j1 = 0; j1 < opt.length; j1++) {
            opt[j1][0] = j1 * GAP_PENALTY;
            //System.out.println(j1 + " " + 0 + " " + opt[j1][0]);
        }

        // recurrence
        for (int i2 = 1; i2 < opt.length; i2++) {
            for (int j2 = 1; j2 < opt[0].length; j2++) {
                // match case
                if (x.charAt(i2-1) == y.charAt(j2-1))
                {
                    opt[i2][j2] = opt[i2-1][j2-1];
                    //System.out.println(i2 + " " + j2 + " " + opt[i2][j2]);
                }
                else
                {
                    opt[i2][j2] = Math.min(
                        Math.min(MISMATCH_PENALTY[SEQUENCE_INDEX.indexOf(x.charAt(i2-1))][SEQUENCE_INDEX
                                .indexOf(y.charAt(j2-1))] + opt[i2 - 1][j2 - 1], GAP_PENALTY + opt[i2 - 1][j2]),
                        GAP_PENALTY + opt[i2][j2 - 1]);
                    //System.out.println(i2 + " " + j2 + " " + opt[i2][j2]);
                }
            }
        }

        // top down pass
        int i = x.length();
        int j = y.length();

        // fixed only this part using https://www.geeksforgeeks.org/sequence-alignment-problem/
        while (i >= 1 && j >= 1)
        {
            // matching pair
            if (x.charAt(i-1) == y.charAt(j-1))
            {
                align1 = x.charAt(i-1) + align1;
                align2 = y.charAt(j-1) + align2;
                i--;
                j--;
                //System.out.println("here1 " + i + " " + j);
            }
            // go diagonal
            else if (opt[i-1][j-1] + MISMATCH_PENALTY[SEQUENCE_INDEX.indexOf(x.charAt(i-1))][SEQUENCE_INDEX.indexOf(y.charAt(j-1))] == opt[i][j])
            {
                align1 = x.charAt(i-1) + align1;
                align2 = y.charAt(j-1) + align2;
                i--;
                j--;
                //System.out.println("here2 " + i + " " + j);
            }
            // x_m go horizontal
            else if (opt[i-1][j] + GAP_PENALTY == opt[i][j])
            {
                align1 = x.charAt(i-1) + align1;
                align2 = "_" + align2;
                i--;
                //System.out.println("here3 " + i + " " + j);
            }
            // y_n go vertical
            else if (opt[i][j-1] + GAP_PENALTY == opt[i][j])
            {
                align2 = y.charAt(j-1) + align2;
                align1 = "_" + align1;
                j--;
                //System.out.println("here4 " + i + " " + j);
            }
            /** 
            else
            {
                System.out.println("error");
            }
            */
        }

        // go down column
        if (j > 0)
        {
            while (j > 0)
            {
                align2 = y.charAt(j-1) + align2;
                align1 = "_" + align1;
                j--;
                //System.out.println("here5 " + i + " " + j);
            }
        }

        // go horizontally
        if (i > 0)
        {
            while (i > 0)
            {
                align1 = x.charAt(i-1) + align1;
                align2 = "_" + align2;
                i--;
                //System.out.println("here6 " + i + " " + j);
            }
        }

        return new String[]{align1, align2};
    }

    public void runEfficient()
    {
        // beginning case
        // floor of x/2
        int xMid = s1.length()/2;
        // substring is O(n) operation
        String xL = s1.substring(0, xMid);
        String xR = s1.substring(xMid);
        String x = s1;
        String y = s2;
        //return divide2(xL, xR, y);
        //divide(x, y);
        String[] alignments = divide(x,y);
        a1 = alignments[0];
        a2 = alignments[1];
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
              //System.out.println("File created: " + myObj.getName());
            } 
            else
            {
              //System.out.println("File already exists.");
            }

            FileWriter myWriter = new FileWriter(output);
            myWriter.write(cost + "\n");
            myWriter.write(a1 + "\n");
            myWriter.write(a2 + "\n");
            myWriter.write(Float.toString(time) + "\n");
            myWriter.write(Float.toString(memory));

            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
        } 
        catch (IOException e) 
        {
            //System.out.println("An error occurred.");
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
            //System.out.println("File not found");
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