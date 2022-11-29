import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

// Class to evaluate a mathematical expression using the CalculatorPanel evaluator functions,
// which are based on Dijkstra's Shunting Yard Algorithm.

public class CalculatorPanelEval {

    // Higher indices indicate higher precedence (precedence = index / 2)
    final static private String[] OPS_PRECEDENCE = { "-", "+", "*", "/", "^", "^",
            "sin", "cos", "tan" , "cot", "ln", "log"};

    // Constructor
    public CalculatorPanelEval()
    {}

    // Evaluate using CalculatorPanel functionality
    public double evaluate(String currExpression)
    {
        // Changed to return to fix error, MUST BE CHANGED
        return Double.parseDouble(infixToRPN(currExpression));
    }

    private static String evalRPN(String expr)
    {
        // Function adapted from: https://rosettacode.org/wiki/Parsing/RPN_calculator_algorithm#Java_2
        // Evaluate a mathematical expression in Reverse Polish Notation (postfix)

        if (expr.equals("Error: Mismatched parenthesis")) return expr;
        LinkedList<Double> stack = new LinkedList<>();

        // Split postfix expression into tokens based on delimiting whitespace
        for (String token : expr.split("\\s")){
            switch (token) {

                // Binary Operators
                case "*" -> {
                    double secondOperand = stack.pop();
                    if (stack.isEmpty()) return "Error: Missing multiplication operand";
                    double firstOperand = stack.pop();
                    stack.push(firstOperand * secondOperand);
                    break;
                }
                case "/" -> {
                    double secondOperand = stack.pop();
                    if (stack.isEmpty()) return "Error: Missing division operand";
                    double firstOperand = stack.pop();
                    if (secondOperand == 0.0) {
                        return "Error: Division by zero is undefined";
                    }
                    stack.push(firstOperand / secondOperand);
                    break;
                }
                case "-" -> {
                    double secondOperand = stack.pop();
                    if (stack.isEmpty()) return "Error: Missing subtraction operand";
                    double firstOperand = stack.pop();
                    stack.push(firstOperand - secondOperand);
                    break;
                }
                case "+" -> {
                    double secondOperand = stack.pop();
                    if (stack.isEmpty()) return "Error: Missing addition operand";
                    double firstOperand = stack.pop();
                    stack.push(firstOperand + secondOperand);
                    break;
                }
                case "^" -> {
                    double secondOperand = stack.pop();
                    if (stack.isEmpty()) return "Error: Missing exponent";
                    double firstOperand = stack.pop();
                    stack.push(Math.pow(firstOperand, secondOperand));
                    break;
                }

                //Unary Operators
                case "sin" -> {
                    if (stack.isEmpty()) return "Error: Incorrect sin expression";
                    double operand = stack.pop();
                    stack.push(Math.sin(operand));
                }
                case "cos" -> {
                    if (stack.isEmpty()) return "Error: Incorrect cos expression";
                    double operand = stack.pop();
                    stack.push(Math.cos(operand));
                }
                case "tan" -> {
                    if (stack.isEmpty()) return "Error: Incorrect tan expression";
                    double operand = stack.pop();
                    stack.push(Math.tan(operand));
                }
                case "cot" -> {
                    if (stack.isEmpty()) return "Error: Incorrect cot expression";
                    double operand = stack.pop();
                    if (operand == 0.0) {
                        return "Error: cot ( 0 ) is undefined";
                    }
                    stack.push(1.0 / Math.tan(operand));
                }
                case "ln" -> {
                    if (stack.isEmpty()) return "Error: Incorrect ln expression";
                    double operand = stack.pop();
                    if (operand <= 0.0) {
                        return "Error: ln ( n <= 0 ) is undefined";
                    }
                    stack.push(Math.log(operand));
                }
                case "log" -> {
                    if (stack.isEmpty()) return "Error: Incorrect log expression";
                    double operand = stack.pop();
                    if (operand <= 0.0) {
                        return "Error: log ( n <= 0 ) is undefined";
                    }
                    stack.push(Math.log(operand));
                }

                // Digits
                default -> {
                    try {
                        stack.push(Double.parseDouble(token + ""));
                    } catch (NumberFormatException e) {
                        return "\nError: invalid token " + token;
                    }
                }
            }
        }
        if (stack.size() > 1) {
            return "Error: too many operands.";
        }

        double result = stack.pop();
        // Check for double overflow
        if (result == Double.POSITIVE_INFINITY || result == Double.NEGATIVE_INFINITY)
            return result + "\nError: Loss of precision due to double overflow.";
        return "" + result;
    }
    static String infixToRPN(String infix)
    {
        // Function adapted from: https://rosettacode.org/wiki/Parsing/Shunting-yard_algorithm#Java
        // Convert infix mathematical expression to Reverse Polish Notation (postfix)

        ArrayList<String> opsPrecedence = new ArrayList<>(Arrays.asList(OPS_PRECEDENCE));
        StringBuilder postfix = new StringBuilder();
        Stack<Integer> s = new Stack<>();

        // Split infix expression into tokens based on delimiting whitespace
        for (String token : infix.split("\\s")) {
            if (token.isEmpty())
                continue;
            int idx = opsPrecedence.indexOf(token);

            // Check if the current token is an operator
            if (idx != -1) {
                if (s.isEmpty()) s.push(idx);
                else {
                    while (!s.isEmpty()) {
                        // If current token's precedence is lower than the operator on top of the stack,
                        // pop the top of the stack and append to result
                        int prec2 = s.peek() / 2;
                        int prec1 = idx / 2;
                        if (prec2 > prec1 || (prec2 == prec1 && !token.equals("^"))) {
                            postfix.append(opsPrecedence.get(s.pop())).append(" ");
                        } else {
                            break;
                        }
                    }
                    s.push(idx);
                }
            }
            else if (token.equals("(")) {
                s.push(-2); // -2 stands for '('
            }
            else if (token.equals("{")) {
                s.push(-4); // -4 stands for '{'
            }
            else if (token.equals(")")) {
                // Until '(' on stack, pop operators.
                while (s.peek() != -2) {
                    if (s.peek() == -4) return "Error: Mismatched parenthesis";
                    postfix.append(opsPrecedence.get(s.pop())).append(" ");
                }
                s.pop();
            }
            else if (token.equals("}")) {
                // Until '(' on stack, pop operators.
                while (s.peek() != -4) {
                    if (s.peek() == -2) return "Error: Mismatched parenthesis";
                    postfix.append(opsPrecedence.get(s.pop())).append(" ");
                }
                s.pop();
            }
            else {
                postfix.append(token).append(' ');
            }
        }
        while (!s.isEmpty())        // Empty out the stack
            postfix.append(opsPrecedence.get(s.pop())).append(' ');
        return postfix.toString();
    }
}
