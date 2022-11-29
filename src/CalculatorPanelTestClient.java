// Oracle evaluator: https://github.com/fathzer/javaluator
// http://javaluator.fathzer.com/en/home/
import javaluator.src.main.java.com.fathzer.soft.javaluator.*;

import java.util.*;
import javax.script.ScriptException;

// This is a unit tester for the CalculatorPanel application, used to generate
// and evaluate randomized test expressions using the CalculatorPanel
// evaluator and an oracle evaluator, then to report on the accuracy
// of the CalculatorPanel application.
public class CalculatorPanelTestClient {

    final static int DIGIT_RANGE = 100000;
    final static String[] OPEN_SYMBOLS = {"(", "{", "digit", "-u", "sin(", "cos(", "tan(" , "cot(", "ln(", "log("};
    final static String[] POST_DIGIT_SYMBOLS = {
            "-", "+", "*", "/", "^", ")", "}"
    };
    final static String[] POST_UNARY_SYMBOLS = {"(", "{", "digit", "-u", "sin(", "cos(", "tan(" , "cot(", "ln(", "log("};
    final static String[] POST_BINARY_SYMBOLS = {"(", "{", "digit", "-u", "sin(", "cos(", "tan(" , "cot(", "ln(", "log("};
    final static String[] POST_OPENP_SYMBOLS = {"(", "{", "digit", "-u", "sin(", "cos(", "tan(" , "cot(", "ln(", "log("};
    final static String[] POST_CLOSEP_SYMBOLS = {")", "}", "-", "+", "*", "/", "^"};

    // Sorted in natural ordering to allow for Arrays.binarySearch()
    final static String[] BINARY_OPERATORS = {"*", "+", "-", "/", "^"};
    final static String[] UNARY_OPERATORS = {"-u", "cos(", "cot(", "ln(", "log(", "sin(", "tan("};
    final static String[] OPEN_PARENS = {"(", "{"};
    final static String[] CLOSE_PARENS = {")", "}"};

    public static void main(String[] args)
    {
        // Create a new evaluator
        DoubleEvaluator evaluator = new DoubleEvaluator();
        String expression = "{cos(sin(ln(tan(tan(cot(tan(cot(38700*ln((-1)*(tan(sin((-1)*{ln(tan(36748))}))))))))))))))}";
        //(cos(sin(ln(tan(tan(cot(tan(cot(38700*ln((-1)*(tan(sin((-1)*(ln(tan(36748)))))))))))))))))
        expression = expression.replace("{", "(").replace("}", ")")
                .replace("cot", "1/tan");
        // Evaluate an expression
        Double result = evaluator.evaluate(expression.substring(0, expression.length() - 2));
        // Output the result
        System.out.println(result);
        //System.out.println(expression + " = " + result);

        System.out.println("""
                Welcome to the CalculatorPanel Test Client.
                This client will evaluate the correctness of Calculator Panel's evaluation function,
                as compared to the "oracle" evaluator Javaluator.
                
                How many test expressions should be generated and evaluated?"
                """);

        Scanner sc = new Scanner(System.in);
        long testCases = sc.nextLong();

        // Create Map of states to allowed input
        Map<String, String[]> inputCheck = new HashMap<>();
        for (String s : BINARY_OPERATORS)   inputCheck.put(s, POST_BINARY_SYMBOLS);
        for (String s : UNARY_OPERATORS)    inputCheck.put(s, POST_UNARY_SYMBOLS);

        inputCheck.put("digit", POST_DIGIT_SYMBOLS);

        for (String s : OPEN_PARENS)        inputCheck.put(s, POST_OPENP_SYMBOLS);
        for (String s : CLOSE_PARENS)       inputCheck.put(s, POST_CLOSEP_SYMBOLS);

        inputCheck.put(".", new String[] {"digit"});

        // Construct test case evaluators:
        CalculatorPanelEval calcEval = new CalculatorPanelEval();
        OracleEval oracleEval = new OracleEval();

        Random r = new Random();

        /*
        // Okay to use long in for loop?
        for (long i = 0; i < testCases; i++)
        {
            String testExpr = generateExpression(r, inputCheck); // Use expression generator here

            // Compare results of different evaluators
            double r1 = calcEval.evaluate(testExpr);
            double r2 = oracleEval.evaluate(testExpr);
        }

        // Provide statistics for overall result of test
        */

        for (int i = 0; i < 5; i++) {
            String s = generateExpression(r, inputCheck);
            System.out.println(s);
        }
    }

    private static String generateExpression(Random r, Map<String, String[]> inputCheck)
    {
        // Construct StringBuilder for generating expression
        StringBuilder sb = new StringBuilder();
        Stack<String> parenStack = new Stack<>();
        // Determine size of generated expression
        int exprSize = r.nextInt(28) + 3;

        // Append opening symbol to expression
        String temp = OPEN_SYMBOLS[r.nextInt(OPEN_SYMBOLS.length)];
        sb.append(temp);

        // Track which parens are being used
        if (Arrays.binarySearch(OPEN_PARENS, temp) >= 0 || Arrays.binarySearch(UNARY_OPERATORS, temp) >= 0)
        {
            if (temp.equals("{"))
                parenStack.push("{");
            else
                parenStack.push("(");
        }

        // Counter for expression length
        int count = 1;
        while (count < exprSize)
        {
            // Add elements to expression based on current available
            // tokens and probability, depending on current state of expression
            String[] options = inputCheck.get(temp);
            temp = options[r.nextInt(options.length)];

            if (Arrays.binarySearch(OPEN_PARENS, temp) >= 0) parenStack.push(temp);

            if ((Arrays.binarySearch(UNARY_OPERATORS, temp) >= 0))  parenStack.push("(");

            if ((Arrays.binarySearch(CLOSE_PARENS, temp) >= 0))
            {
                if (parenStack.size() != 0)
                {
                    String openParen = parenStack.pop();
                    temp = (openParen.equals("(") ? ")" : "}");
                }
                else continue;
            }

            if (temp.equals("digit"))
            {
                sb.append(r.nextInt(DIGIT_RANGE) - DIGIT_RANGE/2);
                count++;
                continue;
            }

            // Access correct input list and symbol selection
            sb.append(temp);
            count++;
        }

        // Append digit if necessary
        if (!temp.equals("digit") || !temp.equals(")") || !temp.equals("}"))
        {
            sb.append(r.nextInt(DIGIT_RANGE) - DIGIT_RANGE/2);
        }

        // Close opened parenthesis
        while (!parenStack.isEmpty())
        {
            String openParen = parenStack.pop();
            temp = openParen.equals("(") ? ")" : "}";
            sb.append(temp);
        }

        String expression = sb.toString().replace("-u", "(-1)*");

        return expression;
    }
}