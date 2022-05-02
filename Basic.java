public class Basic {
    private static int GAP_PENALTY = 30;
    private static int[][] MISMATCH_PENALTY = { { 0, 110, 48, 94 }, { 110, 0, 118, 48 }, { 48, 118, 0, 110 },
            { 94, 48, 110, 0 } };
    
    public static void main(String[] args) {
        String inputPath = args[0];
        String outputPath = args[1];

        System.out.println("CSCI 570 Project " + inputPath + outputPath);
    }
}