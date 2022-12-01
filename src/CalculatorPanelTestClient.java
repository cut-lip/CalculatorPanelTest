// Oracle evaluator: https://github.com/fathzer/javaluator
// http://javaluator.fathzer.com/en/home/
import javaluator.src.main.java.com.fathzer.soft.javaluator.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

// This is a test expression generator for the CalculatorPanel application, used to generate
// randomized test expressions to be evaluated using the CalculatorPanel
// evaluator and the Javaluator "oracle evaluator".
//
// The application reports on the number of expressions evaluated by both evaluators,
// and the number of mathematically incorrect inputs caught by both evaluators.
public class CalculatorPanelTestClient {

    // This global constant indicates the range of integers that will be used to generate expressions
    // [ (-DIGIT_RANGE / 2), (DIGIT_RANGE - DIGIT_RANGE / 2) ]
    final static int DIGIT_RANGE = 100000;

    // These global
    final static String[] OPEN_SYMBOLS = {"(", "{", "digit", "-u", "sin (", "cos (", "tan (" ,
            "cot (", "ln (", "log ("};
    final static String[] POST_DIGIT_SYMBOLS = {".", "-", "+", "*", "/", "^", ")", "}"};
    final static String[] POST_UN_BIN_OPENP_SYMBOLS = {"(", "{", "digit", "-u", "sin (", "cos (",
            "tan (", "cot (", "ln (", "log ("};
    final static String[] POST_CLOSEP_SYMBOLS = {")", "}", "-", "+", "*", "/", "^"};

    // Sorted in natural ordering to allow for Arrays.binarySearch()
    final static String[] BINARY_OPERATORS = {"*", "+", "-", "/", "^"};
    final static String[] UNARY_OPERATORS = {"-u", "cos (", "cot (", "ln (", "log (", "sin (", "tan ("};
    final static String[] OPEN_PARENS = {"(", "{"};
    final static String[] CLOSE_PARENS = {")", "}"};

    public static void main(String[] args)
    {
        System.out.println("""
                Welcome to the CalculatorPanel Test Client.
                
                This client will evaluate the correctness of Calculator Panel's evaluation function,
                as compared to the "oracle" evaluator Javaluator.
                
                Test cases correctly caught and test cases identified as mathematically incorrect
                will be reported for both evaluation methods.
                
                Test cases used will be placed in the file "calc_test_cases.txt"
                in the same directory as the application.
                
                How many test expressions should be generated and evaluated?
                """);

        // Seek user input for number of test cases, checking for correct input
        // (positive whole number)
        long testCases = inputTestCases();

        // Create Map of states to allowed input
        Map<String, String[]> inputCheck = createSymbolMap();

        // Construct test case evaluators:
        // Oracle Evaluator
        DoubleEvaluator evaluator = new DoubleEvaluator();
        Double r1 = null;

        // CalculatorPanel Evaluator
        CalculatorPanelEval calcEval = new CalculatorPanelEval();
        double r2 = 0;

        // Create file "calc_test_cases.txt" to write test cases to
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("calc_test_cases.txt", StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException caught.");
        } catch (IOException e) {
            System.out.println("IOException caught.");
        }
        assert writer != null;

        long oracleCorrect = 0, oracleExceptions = 0, calcPanelCorrect = 0, calcPanelExceptions = 0;

        // Generate test cases and compare results from both evaluators
        for (long i = 0; i < testCases; i++)
        {
            String testExpr = generateExpression(inputCheck); // Use expression generator here
            writer.println(testExpr);
            testExpr = testExpr.replace("{", "(").replace("}", ")")
                    .replace("cot", "1 / tan");

            // Compare results of different evaluators
            // Evaluate an expression
            try {
                r1 = evaluator.evaluate(testExpr);
                ++oracleCorrect;
            } catch (Exception IllegalArgumentException)
            {
                //System.out.println("Mathematically undefined result!");
                ++oracleExceptions;
            }

            try {
                r2 = calcEval.evaluate(testExpr);
                ++calcPanelCorrect;
            } catch (Exception NumberFormatException)
            {
                //System.out.println("Mathematically undefined result!");
                ++calcPanelExceptions;
            }
        }

        writer.close();
        // Provide statistics for overall result of test
        System.out.println("\nOut of " + testCases + " test cases, the oracle evaluated:");
        System.out.println("    " + oracleCorrect + " expressions as mathematically correctly");
        System.out.println("    " + oracleExceptions + " expressions as mathematically incorrect");

        System.out.println("\nOut of " + testCases + " test cases, the calculator evaluated:");
        System.out.println("    " + calcPanelCorrect + " expressions as mathematically correctly");
        System.out.println("    " + calcPanelExceptions + " expressions as mathematically incorrect");

        System.out.println("\nACCURACY STATISTICS:");

        double badClassificationRatio = ((double)oracleCorrect / (double)calcPanelCorrect);
        System.out.println("    CalcPanel incorrectly classified " + badClassificationRatio +
                "% (" + (calcPanelCorrect - oracleCorrect) +
                " out of " + calcPanelCorrect +
                ") test cases as syntactically correct\n    (when compared to Oracle's classification).");
    }

    // SEEK USER INPUT FOR NUMBER OF TEST CASES TO GENERATE
    private static long inputTestCases()
    {
        Scanner sc = new Scanner(System.in);
        boolean gotInput = false;
        long testCases = 0;
        while (!gotInput) {
            try {
                testCases = sc.nextLong();
                if (testCases < 0)
                {
                    System.out.println("Please enter a positive value:");
                    continue;
                }
                gotInput = true;
            } catch (Exception InputMismatchException) {
                System.out.println("Please enter a positive whole number:");
                sc.next();
            }
        }

        return testCases;
    }

    // CONSTRUCT A MAP OF PREVIOUS INPUT TO NEXT ALLOWED INPUTS
    private static Map<String, String[]> createSymbolMap()
    {
        Map<String, String[]> inputCheck = new HashMap<>();
        for (String s : BINARY_OPERATORS)   inputCheck.put(s, POST_UN_BIN_OPENP_SYMBOLS);
        for (String s : UNARY_OPERATORS)    inputCheck.put(s, POST_UN_BIN_OPENP_SYMBOLS);
        for (String s : OPEN_PARENS)        inputCheck.put(s, POST_UN_BIN_OPENP_SYMBOLS);
        for (String s : CLOSE_PARENS)       inputCheck.put(s, POST_CLOSEP_SYMBOLS);
        inputCheck.put("digit", POST_DIGIT_SYMBOLS);

        return inputCheck;
    }

    // GENERATE A RANDOM, SYNTACTICALLY CORRECT MATHEMATICAL EXPRESSION
    private static String generateExpression(Map<String, String[]> inputCheck)
    {
        // Construct StringBuilder for generating expression
        StringBuilder sb = new StringBuilder();
        // Construct Stack to track parenthesis order
        Stack<String> parenStack = new Stack<>();

        Random r = new Random();

        // Randomly determine size of generated expression
        int exprSize = r.nextInt(28) + 3;
        // Append opening symbol to expression
        String temp = OPEN_SYMBOLS[r.nextInt(OPEN_SYMBOLS.length)];

        if (temp.equals("digit"))
        {
            long tempDigit = (r.nextInt(DIGIT_RANGE) - DIGIT_RANGE / 2);
            if (tempDigit < 0)
            {
                // Convert unary negative to "(-1) *" for use with CalculatorPanel Evaluator
                sb.append("( 0 - 1 ) * ");
                sb.append(Math.abs(tempDigit));
                sb.append(" ");
            }
            else
            {
                sb.append(r.nextInt(DIGIT_RANGE) - DIGIT_RANGE / 2);
                sb.append(" ");
            }
        }
        else
        {
            // Track which parens are being used
            if (Arrays.binarySearch(OPEN_PARENS, temp) >= 0 || Arrays.binarySearch(UNARY_OPERATORS, temp) >= 0)
            {
                if (temp.equals("-u")) {
                    // Don't add for unary minus
                } else if (temp.equals("{"))
                    parenStack.push("{");
                else
                    parenStack.push("(");
            }

            sb.append(temp);
            sb.append(" ");
        }

        // Counter for expression length
        int count = 1;
        while (count < exprSize)
        {
            // Add elements to expression based on current available
            // tokens and probability, depending on current state of expression
            String[] options = inputCheck.get(temp);
            temp = options[r.nextInt(options.length)];

            if (temp.equals("."))
            {
                temp = "digit";

                // Remove space in input string for decimal
                sb.deleteCharAt(sb.length() - 1);

                // Append decimal and digit to string
                String tempDec = "." + (r.nextInt(DIGIT_RANGE) + " ");
                sb.append(tempDec);
                count++;
                continue;
            }

            if (temp.equals("digit"))
            {
                long tempDigit = (r.nextInt(DIGIT_RANGE) - DIGIT_RANGE / 2);
                if (tempDigit < 0)
                {
                    // Convert unary negative to "(-1) *" for use with CalculatorPanel Evaluator
                    sb.append("( 0 - 1 ) * ");
                    sb.append(Math.abs(tempDigit));
                    sb.append(" ");
                }
                else
                {
                    sb.append(r.nextInt(DIGIT_RANGE) - DIGIT_RANGE / 2);
                    sb.append(" ");
                    count++;
                }
                continue;
            }

            // Track which parens are being used
            if (Arrays.binarySearch(OPEN_PARENS, temp) >= 0 || Arrays.binarySearch(UNARY_OPERATORS, temp) >= 0)
            {
                if (temp.equals("{"))           parenStack.push("{");
                // Don't add for unary minus
                else if (!temp.equals("-u"))    parenStack.push("(");
            }

            if ((Arrays.binarySearch(CLOSE_PARENS, temp) >= 0))
            {
                if (parenStack.size() != 0)
                {
                    String openParen = parenStack.pop();
                    temp = (openParen.equals("(") ? ")" : "}");
                }
                else continue;
            }

            // Access correct input list and symbol selection
            sb.append(temp);
            sb.append(" ");
            count++;
        }

        // Append digit if necessary
        if (!temp.equals("digit") && !temp.equals(")") && !temp.equals("}"))
        {
            sb.append(r.nextInt(DIGIT_RANGE) - DIGIT_RANGE/2);
            sb.append(" ");
        }

        // Close opened parenthesis
        while (!parenStack.isEmpty())
        {
            String openParen = parenStack.pop();
            temp = openParen.equals("(") ? ")" : "}";
            sb.append(temp);
            sb.append(" ");
        }

        return sb.toString().replace("-u", "( 0 - 1 ) * ");
    }
}