/** Runs dependency-free regression checks for expression input and arithmetic. */
public final class CalculatorModelTest {
    /** Verifies arithmetic, parentheses, unary minus, editing, and recovery sequences. */
    public static void main(String[] args) {
        check("addition", "15", "7+8=");
        check("subtraction", "-1", "7−8=");
        check("multiplication", "56", "7×8=");
        check("division", "0.125", "1÷8=");
        check("decimal precision", "0.3", "0.1+0.2=");
        check("leading decimal", "0.5", ".5");
        check("duplicate decimal", "0.52", ".5.2");
        check("decimal second operand", "2.5", "2+.5=");
        check("operator replacement", "6", "2+×3=");
        check("precedence", "14", "2+3×4=");
        check("grouped sum", "20", "(2+3)×4=");
        check("nested groups", "21", "(1+(2×3))×3=");
        check("leading minus", "-5", "−5=");
        check("negative second operand", "-10", "2×−5=");
        check("subtract negative", "7", "2−−5=");
        check("negative group", "-5", "−(2+3)=");
        check("negative fraction", "-0.5", "−.5=");
        check("backspace then append", "13", "12C3");
        check("clear all", "3", "8+AC3=");
        check("percentage", "0.5", "50%=");
        check("percentage operand", "10", "8+200%=");
        check("group percentage", "0.5", "(20+30)%=");
        check("division by zero", "Error", "8÷0=");
        check("group division by zero", "Error", "8÷(2−2)=");
        check("error recovery", "4", "8÷0=4");
        check("delete error", "0", "8÷0=C");
        check("new number after result", "4", "1+2=4");
        check("continue result", "12", "1+2=×4=");
        check("missing operand", "Error", "2+=");
        check("unclosed group", "Error", "(2+3=");
        check("ignore unmatched closing", "5", "2)+3=");
        check("repeated equals", "5", "2+3==");
        check("input limit", "1234567890123456", "12345678901234567");
        check("delete closing parenthesis", "6", "(2+3)C+1)=");
        check("number times grouped number", "6", "2(3)=");
        check("number times grouped expression", "14", "2(3+4)=");
        check("decimal implicit multiplication", "3.5", ".5(3+4)=");
        check("negative implicit multiplication", "-14", "−2(3+4)=");
        check("nested implicit multiplication", "18", "2(1+2(3+1))=");
        check("adjacent groups", "21", "(1+2)(3+4)=");
        check("addition before group", "9", "2+(3+4)=");
        check("subtraction before group", "-5", "2−(3+4)=");
        check("multiplication before group", "14", "2×(3+4)=");
        check("division before group", "2", "14÷(3+4)=");
        check("implicit multiplication division order", "16", "8÷2(2+2)=");
        check("live leading negative", "-5", "−5");
        check("live negative second operand", "-5", "2×−5");
        check("live subtract negative", "-5", "2−−5");
        check("live subtraction operand", "5", "2−5");
        check("live negative fraction", "-0.5", "−.5");
        check("live negative within group", "-5", "2(−5");
        check("live double unary minus", "5", "−−5");
        check("live triple minus after subtraction", "5", "2−−−5");
        check("edit negative operand", "-1", "−12C");
        check("delete negative operand digit", "-0", "−1C");
        check("pending unary minus", "-0", "2×−");
        CalculatorModel model = new CalculatorModel();
        for (String command : new String[]{"5", "×", "6", "="}) {
            model.accept(command);
        }
        if (!model.getExpression().equals("5×6 =")) {
            throw new AssertionError("Completed expression must retain equals");
        }
        System.out.println("Passed 57 calculator regression checks.");
    }

    /** Checks an input sequence, interpreting AC as one command and other characters individually. */
    private static void check(String name, String expected, String commands) {
        CalculatorModel model = new CalculatorModel();
        for (int index = 0; index < commands.length(); index++) {
            if (commands.startsWith("AC", index)) {
                model.accept("AC");
                index++;
            } else {
                model.accept(String.valueOf(commands.charAt(index)));
            }
        }
        if (!expected.equals(model.getDisplay())) {
            throw new AssertionError(name + ": expected " + expected + ", got " + model.getDisplay());
        }
    }
}
