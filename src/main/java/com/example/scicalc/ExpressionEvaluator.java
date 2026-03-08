package com.example.scicalc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

/**
 * Evaluates scientific math expressions using tokenization + shunting-yard + RPN evaluation.
 * Supports operators (+, -, *, /, ^), parentheses, unary minus, factorial,
 * constants (pi, e), and functions (sin, cos, tan, asin, acos, atan, log, ln, sqrt, abs).
 */
public class ExpressionEvaluator {

    private static final String UNARY_MINUS = "NEG";

    public double evaluate(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression is empty.");
        }

        List<Token> tokens = tokenize(expression);
        List<Token> rpn = toRpn(tokens);
        return evaluateRpn(rpn);
    }

    private List<Token> tokenize(String input) {
        String source = input.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        List<Token> tokens = new ArrayList<>();
        int index = 0;

        while (index < source.length()) {
            char ch = source.charAt(index);

            if (Character.isDigit(ch) || ch == '.') {
                int start = index;
                boolean seenDot = (ch == '.');
                index++;
                while (index < source.length()) {
                    char next = source.charAt(index);
                    if (Character.isDigit(next)) {
                        index++;
                    } else if (next == '.' && !seenDot) {
                        seenDot = true;
                        index++;
                    } else {
                        break;
                    }
                }
                String number = source.substring(start, index);
                tokens.add(Token.number(Double.parseDouble(number)));
                continue;
            }

            if (Character.isLetter(ch)) {
                int start = index;
                index++;
                while (index < source.length() && Character.isLetter(source.charAt(index))) {
                    index++;
                }
                String word = source.substring(start, index);
                if ("pi".equals(word)) {
                    tokens.add(Token.number(Math.PI));
                } else if ("e".equals(word)) {
                    tokens.add(Token.number(Math.E));
                } else {
                    tokens.add(Token.function(word));
                }
                continue;
            }

            switch (ch) {
                case '+', '-', '*', '/', '^', '!' -> {
                    String symbol = String.valueOf(ch);
                    if ("-".equals(symbol) && isUnaryContext(tokens)) {
                        symbol = UNARY_MINUS;
                    }
                    tokens.add(Token.operator(symbol));
                    index++;
                }
                case '(' -> {
                    tokens.add(Token.leftParen());
                    index++;
                }
                case ')' -> {
                    tokens.add(Token.rightParen());
                    index++;
                }
                default -> throw new IllegalArgumentException("Unexpected token: " + ch);
            }
        }

        return tokens;
    }

    private boolean isUnaryContext(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        Token previous = tokens.get(tokens.size() - 1);
        return previous.type == TokenType.OPERATOR
                || previous.type == TokenType.LEFT_PAREN
                || previous.type == TokenType.FUNCTION;
    }

    private List<Token> toRpn(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Stack<Token> operators = new Stack<>();

        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER -> output.add(token);
                case FUNCTION -> operators.push(token);
                case OPERATOR -> {
                    while (!operators.isEmpty() && shouldPop(operators.peek(), token)) {
                        output.add(operators.pop());
                    }
                    operators.push(token);
                }
                case LEFT_PAREN -> operators.push(token);
                case RIGHT_PAREN -> {
                    while (!operators.isEmpty() && operators.peek().type != TokenType.LEFT_PAREN) {
                        output.add(operators.pop());
                    }
                    if (operators.isEmpty() || operators.peek().type != TokenType.LEFT_PAREN) {
                        throw new IllegalArgumentException("Mismatched parentheses.");
                    }
                    operators.pop();
                    if (!operators.isEmpty() && operators.peek().type == TokenType.FUNCTION) {
                        output.add(operators.pop());
                    }
                }
            }
        }

        while (!operators.isEmpty()) {
            Token token = operators.pop();
            if (token.type == TokenType.LEFT_PAREN || token.type == TokenType.RIGHT_PAREN) {
                throw new IllegalArgumentException("Mismatched parentheses.");
            }
            output.add(token);
        }

        return output;
    }

    private boolean shouldPop(Token stackTop, Token incomingOperator) {
        if (stackTop.type == TokenType.FUNCTION) {
            return true;
        }
        if (stackTop.type != TokenType.OPERATOR) {
            return false;
        }

        Operator top = Operator.of(stackTop.text);
        Operator incoming = Operator.of(incomingOperator.text);

        if (incoming.associativity == Associativity.RIGHT) {
            return top.precedence > incoming.precedence;
        }
        return top.precedence >= incoming.precedence;
    }

    private double evaluateRpn(List<Token> rpn) {
        Stack<Double> stack = new Stack<>();

        for (Token token : rpn) {
            switch (token.type) {
                case NUMBER -> stack.push(token.numberValue);
                case FUNCTION -> {
                    double value = popOrThrow(stack);
                    stack.push(applyFunction(token.text, value));
                }
                case OPERATOR -> {
                    Operator operator = Operator.of(token.text);
                    if (operator.arity == 1) {
                        double value = popOrThrow(stack);
                        stack.push(applyUnaryOperator(operator.symbol, value));
                    } else {
                        double right = popOrThrow(stack);
                        double left = popOrThrow(stack);
                        stack.push(applyBinaryOperator(operator.symbol, left, right));
                    }
                }
                default -> throw new IllegalStateException("Unexpected token in RPN: " + token.type);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression.");
        }
        return stack.pop();
    }

    private double popOrThrow(Stack<Double> stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression.");
        }
        return stack.pop();
    }

    private double applyUnaryOperator(String symbol, double value) {
        return switch (symbol) {
            case UNARY_MINUS -> -value;
            case "!" -> factorial(value);
            default -> throw new IllegalArgumentException("Unsupported operator: " + symbol);
        };
    }

    private double applyBinaryOperator(String symbol, double left, double right) {
        return switch (symbol) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> {
                if (right == 0.0d) {
                    throw new IllegalArgumentException("Division by zero.");
                }
                yield left / right;
            }
            case "^" -> Math.pow(left, right);
            default -> throw new IllegalArgumentException("Unsupported operator: " + symbol);
        };
    }

    private double applyFunction(String name, double value) {
        return switch (name) {
            case "sin" -> Math.sin(Math.toRadians(value));
            case "cos" -> Math.cos(Math.toRadians(value));
            case "tan" -> Math.tan(Math.toRadians(value));
            case "asin" -> Math.toDegrees(Math.asin(value));
            case "acos" -> Math.toDegrees(Math.acos(value));
            case "atan" -> Math.toDegrees(Math.atan(value));
            case "log" -> Math.log10(value);
            case "ln" -> Math.log(value);
            case "sqrt" -> Math.sqrt(value);
            case "abs" -> Math.abs(value);
            default -> throw new IllegalArgumentException("Unsupported function: " + name);
        };
    }

    private double factorial(double value) {
        if (value < 0 || value != Math.floor(value)) {
            throw new IllegalArgumentException("Factorial is only defined for non-negative integers.");
        }
        if (value > 170) {
            throw new IllegalArgumentException("Factorial is too large.");
        }

        int n = (int) value;
        double result = 1.0;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private enum TokenType {
        NUMBER,
        OPERATOR,
        FUNCTION,
        LEFT_PAREN,
        RIGHT_PAREN
    }

    private enum Associativity {
        LEFT,
        RIGHT
    }

    private enum Operator {
        PLUS("+", 2, 2, Associativity.LEFT),
        MINUS("-", 2, 2, Associativity.LEFT),
        MULTIPLY("*", 3, 2, Associativity.LEFT),
        DIVIDE("/", 3, 2, Associativity.LEFT),
        POWER("^", 4, 2, Associativity.RIGHT),
        NEGATE(UNARY_MINUS, 5, 1, Associativity.RIGHT),
        FACTORIAL("!", 6, 1, Associativity.LEFT);

        private final String symbol;
        private final int precedence;
        private final int arity;
        private final Associativity associativity;

        Operator(String symbol, int precedence, int arity, Associativity associativity) {
            this.symbol = symbol;
            this.precedence = precedence;
            this.arity = arity;
            this.associativity = associativity;
        }

        static Operator of(String symbol) {
            for (Operator op : values()) {
                if (Objects.equals(op.symbol, symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown operator: " + symbol);
        }
    }

    private static final class Token {
        private final TokenType type;
        private final String text;
        private final double numberValue;

        private Token(TokenType type, String text, double numberValue) {
            this.type = type;
            this.text = text;
            this.numberValue = numberValue;
        }

        static Token number(double value) {
            return new Token(TokenType.NUMBER, null, value);
        }

        static Token operator(String operator) {
            return new Token(TokenType.OPERATOR, operator, 0.0);
        }

        static Token function(String function) {
            return new Token(TokenType.FUNCTION, function, 0.0);
        }

        static Token leftParen() {
            return new Token(TokenType.LEFT_PAREN, "(", 0.0);
        }

        static Token rightParen() {
            return new Token(TokenType.RIGHT_PAREN, ")", 0.0);
        }
    }
}
