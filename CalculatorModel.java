/** Manages calculator input, operand display, completion state, and recoverable errors. */
final class CalculatorModel {
    private static final int MAX_EXPRESSION_LENGTH = 256;
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
        return completed || error ? expression : input;
    }

    /** Applies one shared keyboard or keypad command. */
    void accept(String command) {
        if (command.equals("xʸ")) {
            command = "^";
        }
        if (command.equals("AC")) {
            clear();
        } else if (command.equals("⌫")) {
            deleteCharacter();
        } else if (command.matches("[0-9(]")) {
            if (completed || error) {
                clear();
            }
            if (input.length() < MAX_EXPRESSION_LENGTH) {
                enterValue(command);
            }
        } else if (!error && !command.equals(".") && !command.equals("%")) {
            if (command.equals("=")) {
                finish();
            } else if (command.matches("[+−×÷)^]")) {
                if (completed) {
                    input = display.replace('-', '−');
                    completed = false;
                }
                if (input.length() < MAX_EXPRESSION_LENGTH) {
                    enterSymbol(command);
                }
            } else {
                throw new IllegalArgumentException("Unknown command: " + command);
            }
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

    /** Enters integer digits or opening parentheses, limiting operands to sixteen digits. */
    private void enterValue(String command) {
        String operand = trailingNumber();
        if (command.equals("(")) {
            input += command;
        } else if (!input.endsWith(")") && operand.length() < 16) {
            if (operand.equals("0")) {
                input = input.substring(0, input.length() - 1) + command;
            } else {
                input += command;
            }
        }
        updateOperand();
    }

    /** Handles closing groups, binary operators, and contextual unary minus. */
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
        } else if (command.equals("−")) {
            input += command;
            updateOperand();
        } else if (!expectsOperand()) {
            input += command;
        } else if (!input.isEmpty() && "+×÷^".contains(input.substring(input.length() - 1))) {
            input = input.substring(0, input.length() - 1) + command;
        }
    }

    /** Reports whether the expression requires a number, unary minus, or opening group. */
    private boolean expectsOperand() {
        return input.isEmpty() || "(+−×÷^".contains(input.substring(input.length() - 1));
    }

    /** Extracts the currently entered unsigned integer operand. */
    private String trailingNumber() {
        int start = input.length();
        while (start > 0 && Character.isDigit(input.charAt(start - 1))) {
            start--;
        }
        return input.substring(start);
    }

    /** Shows the current operand with unary signs while keeping subtraction in the expression line. */
    private void updateOperand() {
        String operand = trailingNumber();
        int signEnd = input.length() - operand.length();
        int unarySigns = signEnd - unaryStart(signEnd);
        if (!operand.isEmpty() || unarySigns > 0) {
            display = (unarySigns % 2 == 0 ? "" : "-") + (operand.isEmpty() ? "0" : operand);
        } else if (expectsOperand()) {
            display = "0";
        }
    }

    /** Locates unary minus signs before an operand, excluding a binary subtraction sign. */
    private int unaryStart(int numberStart) {
        int start = numberStart;
        while (start > 0 && input.charAt(start - 1) == '−') {
            start--;
        }
        if (start < numberStart && start > 0
                && "0123456789)".indexOf(input.charAt(start - 1)) >= 0) {
            start++;
        }
        return start;
    }

    /** Evaluates a complete expression and preserves its equals sign above the result. */
    private void finish() {
        if (!input.isEmpty() && !completed) {
            try {
                display = ExpressionEvaluator.evaluate(input).toString();
                expression = input + " =";
                completed = true;
            } catch (ArithmeticException exception) {
                showError("Cannot divide by zero");
            } catch (IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        }
    }

    /** Reports an evaluation failure until clear, delete, or new numeric input. */
    private void showError(String message) {
        display = "Error";
        expression = message;
        error = true;
    }
}
