/** Runs dependency-free regression checks for expression input and arithmetic. */
public final class CalculatorModelTest {
    private static int checks;
    /** Verifies arithmetic, parentheses, unary minus, editing, and recovery sequences. */
    public static void main(String[] args) {
        check("addition", "15", "7+8=");
        check("subtraction", "-1", "7−8=");
        check("multiplication", "56", "7×8=");
        check("division", "0", "1÷8=");
        check("operator replacement", "6", "2+×3=");
        check("precedence", "14", "2+3×4=");
        check("grouped sum", "20", "(2+3)×4=");
        check("nested groups", "21", "(1+(2×3))×3=");
        check("leading minus", "-5", "−5=");
        check("negative second operand", "-10", "2×−5=");
        check("subtract negative", "7", "2−−5=");
        check("negative group", "-5", "−(2+3)=");
        check("backspace then append", "13", "12⌫3");
        check("clear all", "3", "8+AC3=");
        check("division by zero", "Error", "8÷0=");
        check("group division by zero", "Error", "8÷(2−2)=");
        check("error recovery", "4", "8÷0=4");
        check("delete error", "0", "8÷0=⌫");
        check("new number after result", "4", "1+2=4");
        check("continue result", "12", "1+2=×4=");
        check("missing operand", "Error", "2+=");
        check("unclosed group", "Error", "(2+3=");
        check("ignore unmatched closing", "5", "2)+3=");
        check("repeated equals", "5", "2+3==");
        check("input limit", "1234567890123456", "12345678901234567");
        check("delete closing parenthesis", "6", "(2+3)⌫+1)=");
        check("number times grouped number", "6", "2(3)=");
        check("number times grouped expression", "14", "2(3+4)=");
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
        check("live negative within group", "-5", "2(−5");
        check("live double unary minus", "5", "−−5");
        check("live triple minus after subtraction", "5", "2−−−5");
        check("edit negative operand", "-1", "−12⌫");
        check("delete negative operand digit", "-0", "−1⌫");
        check("pending unary minus", "-0", "2×−");
        check("truncated division", "2", "5÷2=");
        check("negative dividend truncation", "-2", "−5÷2=");
        check("negative divisor truncation", "-2", "5÷−2=");
        check("two negative division operands", "2", "−5÷−2=");
        check("truncate each division", "4", "5÷2×2=");
        check("continue negative result", "-1", "2−5=+2=");
        check("continue negative result multiplication", "-6", "2−5=×2=");
        check("no integer overflow", "99999999999999980000000000000001", "9999999999999999×9999999999999999=");
        check("double addition", "5", "2++3=");
        check("double multiplication", "6", "2××3=");
        check("double division", "2", "6÷÷3=");
        check("double subtraction means subtract negative", "5", "2−−3=");
        check("operators before input", "3", "+×÷3=");
        check("empty equals", "0", "=");
        check("empty group", "Error", "()=");
        check("clear error", "3", "1÷0=AC3=");
        check("decimal ignored", "12", "1.2=");
        check("percent ignored", "50", "50%=");
        check("bounded expression evaluation", "Error", "−".repeat(1000) + "=");
        check("integer exponent", "243", "3^5=");
        check("grouped base", "125", "(2+3)^3=");
        check("grouped exponent", "32", "2^(2+3)=");
        check("power precedence", "18", "2×3^2=");
        check("unary minus before power", "-9", "−3^2=");
        check("negative base", "9", "(−3)^2=");
        check("negative base odd power", "-27", "(−3)^3=");
        check("power after result", "125", "2+3=^3=");
        check("right associative power", "512", "2^3^2=");
        check("delete exponent", "9", "3^24⌫=");
        check("implicit multiplication after power", "18", "3^2(2)=");
        check("missing base ignored", "2", "^2=");
        check("power zero", "1", "7^0=");
        check("zero to zero", "1", "0^0=");
        check("zero positive power", "0", "0^3=");
        check("negative exponent rejected", "Error", "2^−3=");
        check("exponent limit", "Error", "2^10001=");
        check("missing exponent", "Error", "2^=");
        check("double exponent key", "8", "2^^3=");
        check("result size limit", "Error", "(2^1000)^10000=");
        check("exponent error recovery", "4", "2^−3=4");
        check("delete last second operand digit", "0", "12+3⌫");
        check("delete opening group", "0", "12+(⌫");
        check("negative result subtract negative", "2", "2−5=−−5=");
        check("keyboard exponent command", "32", "2^5=");
        CalculatorModel model = new CalculatorModel();
        for (String command : new String[]{"5", "×", "6", "="}) {
            model.accept(command);
        }
        if (!model.getExpression().equals("5×6 =")) {
            throw new AssertionError("Completed expression must retain equals");
        }
        checks++;
        model.accept("xʸ");
        model.accept("2");
        model.accept("=");
        if (!model.getDisplay().equals("900")) {
            throw new AssertionError("Power button must use the same operation as the keyboard");
        }
        checks++;
        System.out.println("Passed " + checks + " calculator regression checks.");
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
        checks++;
    }
}
