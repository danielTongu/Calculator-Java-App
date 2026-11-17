import java.math.BigInteger;

/** Parses integer expressions with conventional precedence and nested groups. */
final class ExpressionEvaluator {
    private final String source;
    private int position;

    /** Associates a parser with one immutable expression. */
    private ExpressionEvaluator(String source) {
        this.source = source;
    }

    /** Evaluates one complete expression using a fresh parser with no shared mutable state. */
    static BigInteger evaluate(String expression) {
        return new ExpressionEvaluator(expression).parse();
    }

    /** Evaluates the whole expression and rejects unmatched or trailing input. */
    private BigInteger parse() {
        BigInteger value = sum();
        if (position != source.length()) {
            throw new IllegalArgumentException("Unexpected input");
        }
        return value;
    }

    /** Evaluates addition and subtraction after higher-precedence operations. */
    private BigInteger sum() {
        BigInteger value = product();
        while (position < source.length() && "+−".indexOf(source.charAt(position)) >= 0) {
            char operator = source.charAt(position++);
            BigInteger operand = product();
            if (operator == '+') {
                value = Arithmetic.addition(value, operand);
            } else {
                value = Arithmetic.subtraction(value, operand);
            }
        }
        return value;
    }

    /** Evaluates explicit or implicit multiplication and truncated integer division from left to right. */
    private BigInteger product() {
        BigInteger value = unary();
        while (position < source.length() && "×÷(".indexOf(source.charAt(position)) >= 0) {
            char operator = source.charAt(position);
            if (operator != '(') {
                position++;
            }
            BigInteger operand = unary();
            if (operator == '÷') {
                value = Arithmetic.division(value, operand);
            } else {
                value = Arithmetic.multiplication(value, operand);
            }
        }
        return value;
    }

    /** Evaluates contextual unary minus before returning to binary arithmetic. */
    private BigInteger unary() {
        BigInteger value;
        if (take('−')) {
            value = unary().negate();
        } else {
            value = power();
        }
        return value;
    }

    /** Evaluates exponentiation from right to left, before unary negation. */
    private BigInteger power() {
        BigInteger value = primary();
        if (take('^')) {
            value = Arithmetic.exponentiation(value, unary());
        }
        return value;
    }

    /** Reads an integer or parenthesized expression. */
    private BigInteger primary() {
        BigInteger value;
        if (take('(')) {
            value = sum();
            if (!take(')')) {
                throw new IllegalArgumentException("Unclosed parenthesis");
            }
        } else {
            int start = position;
            while (position < source.length()
                    && Character.isDigit(source.charAt(position))) {
                position++;
            }
            if (start == position) {
                throw new IllegalArgumentException("Expected operand");
            }
            value = new BigInteger(source.substring(start, position));
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
