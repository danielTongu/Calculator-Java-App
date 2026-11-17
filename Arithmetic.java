import java.math.BigInteger;

/**
 * Provides the four integer arithmetic operations required by Lab 1.
 * Also supports bounded exponentiation for the optional power key.
 *
 * @author Daniel Tongu
 */
public final class Arithmetic {

    /** Prevents instantiation of this stateless utility class. */
    private Arithmetic() {
    }

    /**
     * Adds two integers without fixed-width overflow.
     * @param first the first operand
     * @param second the second operand
     * @return the sum
     */
    public static BigInteger addition(BigInteger first, BigInteger second) {
        return first.add(second);
    }

    /**
     * Subtracts the second integer from the first.
     * @param first the minuend
     * @param second the subtrahend
     * @return the difference
     */
    public static BigInteger subtraction(BigInteger first, BigInteger second) {
        return first.subtract(second);
    }

    /**
     * Multiplies two integers without fixed-width overflow.
     * @param first the first operand
     * @param second the second operand
     * @return the product
     */
    public static BigInteger multiplication(BigInteger first, BigInteger second) {
        return first.multiply(second);
    }

    /**
     * Divides integers, discarding the fractional part toward zero.
     * @param first the dividend
     * @param second the divisor
     * @return the integer quotient; for example, 5 / 2 is 2 and -5 / 2 is -2
     * @throws ArithmeticException if the divisor is zero
     */
    public static BigInteger division(BigInteger first, BigInteger second) {
        return first.divide(second);
    }

    /**
     * Raises an integer base to a nonnegative integer exponent; zero to zero is one.
     * @param base the integer base
     * @param exponent the exponent, from zero through ten thousand
     * @return the integer power
     * @throws IllegalArgumentException if the exponent is outside the supported range
     *         or the estimated result exceeds one million bits
     */
    public static BigInteger exponentiation(BigInteger base, BigInteger exponent) {
        if (exponent.signum() < 0 || exponent.compareTo(BigInteger.valueOf(10000)) > 0) {
            throw new IllegalArgumentException("Exponent must be between 0 and 10000");
        }
        int power = exponent.intValue();
        if ((long) base.abs().bitLength() * power > 1000000) {
            throw new IllegalArgumentException("Power result is too large");
        }
        return base.pow(power);
    }
}
