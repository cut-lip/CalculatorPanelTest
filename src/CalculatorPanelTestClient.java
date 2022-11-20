import java.util.Scanner;

// This is a unit tester for the CalculatorPanel application, used to generate
// and evaluate randomized test expressions using the CalculatorPanel
// evaluator and an oracle evaluator, then to report on the accuracy
// of the CalculatorPanel application.
public class CalculatorPanelTestClient {

    public static void main(String[] args)
    {
        System.out.println("Welcome to the CalculatorPanel Test Client.\n"
                            + "This client will evaluate the correctness of Calculator Panel's evaluation function,\n"
                            + "as compared to the \"oracle\" evaluator __________.\n");

        System.out.println("How many test expressions should be generated and evaluated? ");
        Scanner s = new Scanner(System.in);
        // Is a long really necessary here?
        long testCases = s.nextLong();

        // Construct test case generators.:
        CalculatorPanelEval calcEval = new CalculatorPanelEval();
        OracleEval oracleEval = new OracleEval();
        // Okay to use long in for loop?
        for (long i = 0; i < testCases; i++)
        {
            String testExpr = ""; // Use expression generator here

            // Compare results of different evaluators
            double r1 = calcEval.evaluate(testExpr);
            double r2 = calcEval.evaluate(testExpr);
        }

        // Provide statistics for overall result of test
    }


}