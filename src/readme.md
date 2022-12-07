# Welcome to CalculatorPanelTestClient

### This program is a test expression generator for the CalculatorPanel application.
### CalculatorPanelTestClient generates a user-defined number of mathematical expressions as test cases to be evaluated on the CalculatorPanel evaluation algorithm, and then compared to evaluations performed by the Javaluator oracle evaluator
Oracle Source Code: https://github.com/fathzer/javaluator
### Statistics are reported on the console, and test cases generated are output to a text file in the current directory named "calc_test_cases.txt"

##

### Compilation instructions (from within the command prompt):

Navigate to the directory containing CalculatorPanelTestClient.java, CalculatorPanelEval.java, and the Javaluator Oracle evaluator library with **cd**.

Next, compile the files with the command

    javac CalculatorPanelTestClient.java CalculatorPanelEval.java
Now you can launch the application with the command:

    java CalculatorPanelTestClient

##
### Important Implementation Details:

Note that the high number of mathematically incorrect test cases for each evaluation method are due to the high probability that a longer mathematical expression will contain a logarithm function with negative input, which is mathematically undefined.

In order to maintain compatibility with the CalculatorPanel evaluation method, automatically generated test cases will be modulated such that unary negatives are converted to a format compatible with CalculatorPanel.

Trigonometric functions are evaluated in radians.

Range of digits randomly produced for generated expressions is between -50,000,000 and 50,000,000.

Size of expression is randomly determined for each test case, up to 30 independent symbols.

"calc_test_cases.txt" is output file containing test cases.