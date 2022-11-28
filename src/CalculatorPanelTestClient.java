import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;


// This is a unit tester for the CalculatorPanel application, used to generate
// and evaluate randomized test expressions using the CalculatorPanel
// evaluator and an oracle evaluator, then to report on the accuracy
// of the CalculatorPanel application.
public class CalculatorPanelTestClient {

    // Digits
    final static String[] DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};

    // Opening Symbols:
    final static String[] OPEN_SYMBOLS = {"(", "{", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "-", "sin", "cos", "tan" , "cot", "ln", "log"};

    // Binary operators
    final static String[] BINARY_OPERATORS= { "-", "+", "*", "/", "^", "^"};

    // Unary operators
    final static String[] UNARY_OPERATORS = {"-", "sin", "cos", "tan" , "cot", "ln", "log"};


    public static void main(String[] args)
    {
        System.out.println("Welcome to the CalculatorPanel Test Client.\n"
                            + "This client will evaluate the correctness of Calculator Panel's evaluation function,\n"
                            + "as compared to the \"oracle\" evaluator __________.\n");

        System.out.println("How many test expressions should be generated and evaluated? ");
        Scanner s = new Scanner(System.in);
        // Is a long really necessary here?
        long testCases = s.nextLong();

        // Construct test case evaluators:
        CalculatorPanelEval calcEval = new CalculatorPanelEval();
        OracleEval oracleEval = new OracleEval();

        Random r = new Random();
        // Okay to use long in for loop?
        for (long i = 0; i < testCases; i++)
        {
            ArrayList<String> testExpr = generateExpression(r); // Use expression generator here

            // Compare results of different evaluators
            double r1 = calcEval.evaluate(testExpr);
            double r2 = oracleEval.evaluate(testExpr);
        }

        // Provide statistics for overall result of test
    }

    private static ArrayList<String> generateExpression(Random r)
    {
        // ArrayList or String?

        // Generate a random expression
        // Randomize by SIZE, ORDER, and COMPLEXITY
        int exprSize = r.nextInt(30) + 1;

        // Counter for expression length
        int count = 0;

        // Placeholder for randomly generated numbers
        int tempRand = 0;

        while (count < exprSize)
        {
            // Add elements to expression based on current available
            // tokens and probability:
            // starting with opening symbol
            tempRand = r.nextInt(OPEN_SYMBOLS.length());

            count++;
        }

        return new ArrayList<String>();
    }

}