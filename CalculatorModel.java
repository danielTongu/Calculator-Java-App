import java.math.BigDecimal;
import java.math.MathContext;

/** Edits arithmetic expressions and evaluates parentheses, precedence, and unary minus. */
final class CalculatorModel {
    private String input = "";
    private String display = "0";
    private String expression = "";
    private boolean completed;
    private boolean error;

    /** Returns the current operand or evaluated result. */
    String getDisplay() {
        return display;
    }

    /** Returns the entered expression, including equals after successful evaluation. */
    String getExpression() {
        return expression;
    }

    /** Applies one shared keyboard or keypad command. */
    void accept(String command) {
        if (command.equals("AC")) {
            clear();
        } else if (command.equals("C") || command.equals("⌫")) {
            deleteCharacter();
        } else if (command.matches("[0-9.(]")) {
            if (completed || error) {
                clear();
            }
            enterValue(command);
        } else if (!error) {
            if (command.equals("=")) {
                finish();
            } else if (command.matches("[+−×÷)%]")) {
                if (completed) {
                    input = display;
                    completed = false;
                }
                enterSymbol(command);
            } else {
                throw new IllegalArgumentException("Unknown command: " + command);
            }
        }
        if (!completed && !error) {
            expression = input;
        }
    }

    /** Restores an empty expression and zero display. */
    private void clear() {
        input = "";
        display = "0";
        expression = "";
        completed = false;
        error = false;
    }

    /** Deletes an expression character or clears a completed result or error. */
    private void deleteCharacter() {
        if (completed || error) {
            clear();
        } else if (!input.isEmpty()) {
            input = input.substring(0, input.length() - 1);
            updateOperand();
        }
    }

    /** Enters digits, decimal points, or explicit opening parentheses with a digit limit. */
    private void enterValue(String command) {
        String operand = trailingNumber();
        if (command.equals("(")) {
            input += command;
        } else if (!input.endsWith(")") && !input.endsWith("%")) {
            if (command.equals(".")) {
                if (!operand.contains(".")) {
                    input += operand.isEmpty() ? "0." : ".";
                }
            } else if (operand.replace(".", "").length() < 16) {
                if (operand.equals("0")) {
                    input = input.substring(0, input.length() - 1) + command;
                } else {
                    input += command;
                }
            }
        }
        updateOperand();
    }

    /** Handles closing groups, percentages, binary operators, and contextual unary minus. */
    private void enterSymbol(String command) {
        if (command.equals(")")) {
            int balance = 0;
            for (int index = 0; index < input.length(); index++) {
                if (input.charAt(index) == '(') {
                    balance++;
                } else if (input.charAt(index) == ')') {
                    balance--;
                }
            }
            if (!expectsOperand() && balance > 0) {
                input += command;
            }
        } else if (command.equals("%")) {
            if (!expectsOperand()) {
                input += command;
            }
        } else if (command.equals("−")) {
            input += command;
            updateOperand();
        } else if (!expectsOperand()) {
            input += command;
        } else if (!input.isEmpty() && "+×÷".contains(input.substring(input.length() - 1))) {
            input = input.substring(0, input.length() - 1) + command;
        }
    }

    /** Reports whether the expression requires a number, unary minus, or opening group. */
    private boolean expectsOperand() {
        return input.isEmpty() || "(+−×÷".contains(input.substring(input.length() - 1));
    }

    /** Extracts the currently entered unsigned decimal operand. */
    private String trailingNumber() {
        int start = input.length();
        while (start > 0 && (Character.isDigit(input.charAt(start - 1)) || input.charAt(start - 1) == '.')) {
            start--;
        }
        return input.substring(start);
    }

    /** Shows the current operand with unary signs while keeping subtraction in the expression line. */
    private void updateOperand() {
        String operand = trailingNumber();
        int signEnd = input.length() - operand.length();
        int signStart = signEnd;
        while (signStart > 0 && input.charAt(signStart - 1) == '−') {
            signStart--;
        }
        int unarySigns = signEnd - signStart;
        if (unarySigns > 0 && signStart > 0
                && "0123456789.)%".indexOf(input.charAt(signStart - 1)) >= 0) {
            unarySigns--;
        }
        if (!operand.isEmpty() || unarySigns > 0) {
            display = (unarySigns % 2 == 0 ? "" : "-") + (operand.isEmpty() ? "0" : operand);
        } else if (input.isEmpty()) {
            display = "0";
        }
    }

    /** Evaluates a complete expression and preserves its equals sign above the result. */
    private void finish() {
        if (!input.isEmpty() && !completed) {
            try {
                display = new ExpressionParser(input).parse().stripTrailingZeros().toPlainString();
                expression = input + " =";
                completed = true;
            } catch (ArithmeticException exception) {
                showError("Cannot divide by zero");
            } catch (IllegalArgumentException exception) {
                showError("Incomplete or invalid expression");
            }
        }
    }

    /** Reports an evaluation failure until clear, delete, or new numeric input. */
    private void showError(String message) {
        display = "Error";
        expression = message;
        error = true;
    }

    /** Parses decimal expressions with conventional precedence and nested groups. */
    private static final class ExpressionParser {
        private final String source;
        private int position;

        /** Associates a parser with one immutable expression. */
        ExpressionParser(String source) {
            this.source = source;
        }

        /** Evaluates the whole expression and rejects unmatched or trailing input. */
        BigDecimal parse() {
            BigDecimal value = sum();
            if (position != source.length()) {
                throw new IllegalArgumentException("Unexpected input");
            }
            return value;
        }

        /** Evaluates addition and subtraction after higher-precedence operations. */
        private BigDecimal sum() {
            BigDecimal value = product();
            while (position < source.length() && "+−".indexOf(source.charAt(position)) >= 0) {
                char operator = source.charAt(position++);
                BigDecimal operand = product();
                if (operator == '+') {
                    value = value.add(operand);
                } else {
                    value = value.subtract(operand);
                }
            }
            return value;
        }

        /** Evaluates explicit or implicit multiplication and decimal division from left to right. */
        private BigDecimal product() {
            BigDecimal value = unary();
            while (position < source.length() && "×÷(".indexOf(source.charAt(position)) >= 0) {
                char operator = source.charAt(position);
                if (operator != '(') {
                    position++;
                }
                BigDecimal operand = unary();
                if (operator == '÷') {
                    value = value.divide(operand, MathContext.DECIMAL64);
                } else {
                    value = value.multiply(operand);
                }
            }
            return value;
        }

        /** Evaluates contextual unary minus before returning to binary arithmetic. */
        private BigDecimal unary() {
            BigDecimal value;
            if (take('−')) {
                value = unary().negate();
            } else {
                value = primary();
            }
            return value;
        }

        /** Reads a decimal or parenthesized expression followed by optional percentages. */
        private BigDecimal primary() {
            BigDecimal value;
            if (take('(')) {
                value = sum();
                if (!take(')')) {
                    throw new IllegalArgumentException("Unclosed parenthesis");
                }
            } else {
                int start = position;
                while (position < source.length()
                        && (Character.isDigit(source.charAt(position)) || source.charAt(position) == '.')) {
                    position++;
                }
                if (start == position) {
                    throw new IllegalArgumentException("Expected operand");
                }
                value = new BigDecimal(source.substring(start, position));
            }
            while (take('%')) {
                value = value.movePointLeft(2);
            }
            return value;
        }

        /** Consumes an expected character when it is the next input symbol. */
        private boolean take(char symbol) {
            boolean matches = position < source.length() && source.charAt(position) == symbol;
            if (matches) {
                position++;
            }
            return matches;
        }
    }
}
