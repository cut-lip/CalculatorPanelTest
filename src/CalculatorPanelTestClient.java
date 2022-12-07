// Oracle evaluator: https://github.com/fathzer/javaluator
// http://javaluator.fathzer.com/en/home/
import javaluator.src.main.java.com.fathzer.soft.javaluator.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    final static long DIGIT_RANGE = 100000000;

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

        // Create file "calc_test_cases.txt" to write test cases to
        PrintWriter writer = createOutputWriter();

        // Generate test cases and compare results from both evaluators
        // stats: [0]=oracleCorrect, [1]=oracleExceptions, [2]=calcPanelCorrect,
        // [3]=calcPanelExceptions, [4]=wrongEval
        long[] stats = compareEvaluators(testCases, writer);

        // Close file being written to
        writer.close();

        // Provide statistics for overall result of test
        reportStatistics(testCases, stats);
    }

    // SEEK USER INPUT FOR NUMBER OF TEST CASES TO GENERATE
    private static long inputTestCases()
    {
        Scanner sc = new Scanner(System.in);
        boolean gotInput = false;
        long testCases = 0;

        // Check for correct input (positive whole number)
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

    private static PrintWriter createOutputWriter()
    {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("calc_test_cases.txt", StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException caught.");
        } catch (IOException e) {
            System.out.println("IOException caught.");
        }

        assert writer != null;
        return writer;
    }

    // COMPARE THE EVALUATIONS OF THE ORACLE AND THE CALCULATOR ON THE TEST CASES
    private static long[] compareEvaluators(long testCases, PrintWriter writer)
    {
        // Create Map of states to allowed input
        Map<String, String[]> inputCheck = createSymbolMap();

        // Construct test case evaluators:
        // Oracle Evaluator
        DoubleEvaluator evaluator = new DoubleEvaluator();
        Double r1 = null;
        // CalculatorPanel Evaluator
        CalculatorPanelEval calcEval = new CalculatorPanelEval();
        double r2 = 0;

        // stats: [0]=oracleCorrect, [1]=oracleExceptions, [2]=calcPanelCorrect,
        // [3]=calcPanelExceptions, [4]=wrongEval
        long[] stats = new long[5];

        // Flags to determine if both evaluators evaluated a given expression
        boolean calcEvaled = false, oracleEvaled = false;

        // Generate and compare test expressions
        for (long i = 0; i < testCases; i++)
        {
            String testExpr = generateExpression(inputCheck);   // Generate expression
            writer.println(testExpr);                           // Write Expression to file

            // Format expression for evaluators
            testExpr = testExpr.replace("{", "(").replace("}", ")")
                    .replace("cot", "1 / tan");

            // Compare results of different evaluators
            try {
                r1 = evaluator.evaluate(testExpr);      // Oracle evaluator
                ++stats[0];
                oracleEvaled = true;
            } catch (IllegalArgumentException e)
            {
                ++stats[1];     // Mathematically undefined result!
            }
            try {
                r2 = calcEval.evaluate(testExpr);       // Calculator evaluator
                ++stats[2];
                calcEvaled = true;
            } catch (NumberFormatException e)
            {
                ++stats[3];     // Mathematically undefined result!
            }

            // Determine if the calculator returned the same value as the oracle
            if ((oracleEvaled && calcEvaled) && (r1 != r2))
            {
                ++stats[4];
                oracleEvaled = false;
                calcEvaled = false;
            }
        }

        return stats;
    }

    // CALCULATE AND DISPLAY STATISTICS OF THE EVALUATION ON THE CONSOLE
    private static void reportStatistics(long testCases, long[] stats)
    {
        // stats: [0]=oracleCorrect, [1]=oracleExceptions, [2]=calcPanelCorrect,
        // [3]=calcPanelExceptions, [4]=wrongEval
        System.out.println("\nOut of " + testCases + " test cases, the oracle evaluated:");
        System.out.println("    " + stats[0] + " expressions as mathematically correctly");
        System.out.println("    " + stats[1] + " expressions as mathematically incorrect");

        System.out.println("\nOut of " + testCases + " test cases, the calculator evaluated:");
        System.out.println("    " + stats[2] + " expressions as mathematically correctly");
        System.out.println("    " + stats[3] + " expressions as mathematically incorrect");

        System.out.println("\nACCURACY STATISTICS:");

        double badClassRatio = 100 - (100.0 * ((double)stats[0] / (double)stats[2]));
        double badClassRatioRound = round(badClassRatio, 2);
        System.out.println("    CalcPanel incorrectly classified " + badClassRatioRound +
                "% (" + (stats[2] - stats[0]) +
                " out of " + stats[2] +
                ") test cases as syntactically correct\n    (when compared to Oracle's classification).");

        double badEvalRatio = 100.0 * ((double)stats[4] / (double)stats[0]);
        double badEvalRatioRound = round(badEvalRatio, 2);
        System.out.println("\n    CalcPanel incorrectly evaluated " + badEvalRatioRound +
                "% (" + stats[4] +
                " out of " + stats[0] +
                ") test cases.\n    (when compared to Oracle's evaluation).");

    }

    // ROUND DOUBLE VALUES TO TWO DECIMAL PLACES
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
            long tempDigit = (r.nextLong(DIGIT_RANGE) - DIGIT_RANGE / 2);
            if (tempDigit < 0)
            {
                // Convert unary negative to "(-1) *" for use with CalculatorPanel Evaluator
                sb.append("( 0 - 1 ) * ");
                sb.append(Math.abs(tempDigit));
                sb.append(" ");
            }
            else
            {
                sb.append(r.nextLong(DIGIT_RANGE) - DIGIT_RANGE / 2);
                sb.append(" ");
            }
        }
        else
        {
            // Track which parens are being used
            if (Arrays.binarySearch(OPEN_PARENS, temp) >= 0 || Arrays.binarySearch(UNARY_OPERATORS, temp) >= 0)
            {
                switch (temp) {
                    case "-u":
                        break;
                    case "{":
                        parenStack.push("{");
                        break;
                    default:
                        parenStack.push("(");
                        break;
                }
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
                String tempDec = "." + (r.nextLong(DIGIT_RANGE) + " ");
                sb.append(tempDec);
                count++;
                continue;
            }

            if (temp.equals("digit"))
            {
                long tempDigit = (r.nextLong(DIGIT_RANGE) - DIGIT_RANGE / 2);
                if (tempDigit < 0)
                {
                    // Convert unary negative to "(-1) *" for use with CalculatorPanel Evaluator
                    sb.append("( 0 - 1 ) * ");
                    sb.append(Math.abs(tempDigit));
                    sb.append(" ");
                }
                else
                {
                    sb.append(r.nextLong(DIGIT_RANGE) - DIGIT_RANGE / 2);
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
            sb.append(r.nextLong(DIGIT_RANGE) - DIGIT_RANGE/2);
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