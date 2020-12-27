package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static java.math.BigDecimal.*;

/**
 * Provides advanced functions operating on {@link BigDecimal}s.
 * Taken from <a href="github.com/eobermuhlner/big-math/blob/master/ch.obermuhlner.math.big/src/main/java/ch/obermuhlner/math/big/BigDecimalMath.java">@obermuhlner's Github</a>
 *
 * I do not claim ownership of this code, it is the intellectual property of <a href="github.com/eobermuhlner">@obermuhlner</a>.
 * @author @obermuhlner
 */
public class BigDecimalMath {

    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final MathContext DEFAULT_CONTEXT = new MathContext(20, DEFAULT_ROUNDING_MODE);
    private static final BigDecimal TWO = valueOf(2);
    private static final BigDecimal THREE = valueOf(3);
    private static final BigDecimal MINUS_ONE = valueOf(-1);
    private static final BigDecimal ONE_HALF = valueOf(0.5);

    private static final BigDecimal DOUBLE_MAX_VALUE = BigDecimal.valueOf(Double.MAX_VALUE);
    private static final Object log2CacheLock = new Object();
    private static final Object log3CacheLock = new Object();
    private static final Object log10CacheLock = new Object();
    private static final Object piCacheLock = new Object();
    private static final Object eCacheLock = new Object();
    private static final BigDecimal ROUGHLY_TWO_PI = new BigDecimal("3.141592653589793").multiply(TWO);
    private static final int EXPECTED_INITIAL_PRECISION = 15;
    private static final Map<Integer, List<BigDecimal>> spougeFactorialConstantsCache = new HashMap<>();
    private static final Object spougeFactorialConstantsCacheLock = new Object();
    private static volatile BigDecimal log2Cache;
    private static volatile BigDecimal log3Cache;
    private static volatile BigDecimal log10Cache;
    private static volatile BigDecimal piCache;
    private static final BigDecimal[] factorialCache = new BigDecimal[100];
    private static BigDecimal eCache;

    static {
        var result = ONE;
        factorialCache[0] = result;
        for (var i = 1; i < factorialCache.length; i++) {
            result = result.multiply(valueOf(i));
            factorialCache[i] = result;
        }
    }

    private BigDecimalMath() {
        // prevent instances
    }

    /**
     * Returns whether the specified {@link BigDecimal} value can be represented as <code>int</code>.
     *
     * <p>If this returns <code>true</code> you can call {@link BigDecimal#intValueExact()} without fear of an {@link ArithmeticException}.</p>
     *
     * @param value the {@link BigDecimal} to check
     * @return <code>true</code> if the value can be represented as <code>int</code> value
     */
    public static boolean isIntValue(BigDecimal value) {
        return value.compareTo(ZERO) == 0
                || value.scale() <= 0
                || value.stripTrailingZeros().scale() <= 0;
    }

    /**
     * Returns whether the specified {@link BigDecimal} value can be represented as <code>double</code>.
     *
     * <p>If this returns <code>true</code> you can call {@link BigDecimal#doubleValue()}
     * without fear of getting {@link Double#POSITIVE_INFINITY} or {@link Double#NEGATIVE_INFINITY} as result.</p>
     *
     * <p>Example: <code>BigDecimalMath.isDoubleValue(new BigDecimal("1E309"))</code> returns <code>false</code>,
     * because <code>new BigDecimal("1E309").doubleValue()</code> returns <code>Infinity</code>.</p>
     *
     * <p>Note: This method does <strong>not</strong> check for possible loss of precision.</p>
     *
     * <p>For example <code>BigDecimalMath.isDoubleValue(new BigDecimal("1.23400000000000000000000000000000001"))</code> will return <code>true</code>,
     * because <code>new BigDecimal("1.23400000000000000000000000000000001").doubleValue()</code> returns a valid double value,
     * although it loses precision and returns <code>1.234</code>.</p>
     *
     * <p><code>BigDecimalMath.isDoubleValue(new BigDecimal("1E-325"))</code> will return <code>true</code>
     * although this value is smaller than {@link Double#MIN_VALUE} (and therefore outside the range of values that can be represented as <code>double</code>)
     * because <code>new BigDecimal("1E-325").doubleValue()</code> returns <code>0</code> which is a legal value with loss of precision.</p>
     *
     * @param value the {@link BigDecimal} to check
     * @return <code>true</code> if the value can be represented as <code>double</code> value
     */
    public static boolean isDoubleValue(BigDecimal value) {
        return value.compareTo(DOUBLE_MAX_VALUE) <= 0 && value.compareTo(DOUBLE_MAX_VALUE.negate()) >= 0;
    }

    /**
     * Returns the mantissa of the specified {@link BigDecimal} written as <em>mantissa * 10<sup>exponent</sup></em>.
     *
     * <p>The mantissa is defined as having exactly 1 digit before the decimal point.</p>
     *
     * @param value the {@link BigDecimal}
     * @return the mantissa
     * @see #exponent(BigDecimal)
     */
    public static BigDecimal mantissa(BigDecimal value) {
        var exponent = exponent(value);
        if (exponent == 0) {
            return value;
        }

        return value.movePointLeft(exponent);
    }

    /**
     * Returns the exponent of the specified {@link BigDecimal} written as <em>mantissa * 10<sup>exponent</sup></em>.
     *
     * <p>The mantissa is defined as having exactly 1 digit before the decimal point.</p>
     *
     * @param value the {@link BigDecimal}
     * @return the exponent
     * @see #mantissa(BigDecimal)
     */
    public static int exponent(BigDecimal value) {
        return value.precision() - value.scale() - 1;
    }

    /**
     * Returns the integral part of the specified {@link BigDecimal} (left of the decimal point).
     *
     * @param value the {@link BigDecimal}
     * @return the integral part
     * @see #fractionalPart(BigDecimal)
     */
    public static BigDecimal integralPart(BigDecimal value) {
        return value.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Returns the fractional part of the specified {@link BigDecimal} (right of the decimal point).
     *
     * @param value the {@link BigDecimal}
     * @return the fractional part
     * @see #integralPart(BigDecimal)
     */
    public static BigDecimal fractionalPart(BigDecimal value) {
        return value.subtract(integralPart(value));
    }

    /**
     * Rounds the specified {@link BigDecimal} to the precision of the specified {@link MathContext}.
     *
     * <p>This method calls {@link BigDecimal#round(MathContext)}.</p>
     *
     * @param value       the {@link BigDecimal} to round
     * @param mathContext the {@link MathContext} used for the result
     * @return the rounded {@link BigDecimal} value
     * @see BigDecimal#round(MathContext)
     */
    public static BigDecimal round(BigDecimal value, MathContext mathContext) {
        return value.round(mathContext);
    }

    /**
     * Calculates the reciprocal of the specified {@link BigDecimal}.
     *
     * @param x           the {@link BigDecimal}
     * @param mathContext the {@link MathContext} used for the result
     * @return the reciprocal {@link BigDecimal}
     * @throws ArithmeticException if x = 0
     * @throws ArithmeticException if the result is inexact but the
     *                             rounding mode is {@code UNNECESSARY} or
     *                             {@code mc.precision == 0} and the quotient has a
     *                             non-terminating decimal expansion.
     */
    public static BigDecimal reciprocal(BigDecimal x, MathContext mathContext) {
        return BigDecimal.ONE.divide(x, mathContext);
    }

    /**
     * Calculates the factorial of the specified integer argument.
     *
     * <p>factorial = 1 * 2 * 3 * ... n</p>
     *
     * @param n the {@link BigDecimal}
     * @return the factorial {@link BigDecimal}
     * @throws ArithmeticException if x &lt; 0
     */
    public static BigDecimal factorial(int n) {
        if (n < 0) {
            throw new ArithmeticException("Illegal factorial(n) for n < 0: n = " + n);
        }
        if (n < factorialCache.length) {
            return factorialCache[n];
        }

        var result = factorialCache[factorialCache.length - 1];
        return result.multiply(factorialRecursion(factorialCache.length, n));
    }

    private static BigDecimal factorialLoop(int n1, final int n2) {
        final var limit = Long.MAX_VALUE / n2;
        long accu = 1;
        var result = BigDecimal.ONE;
        while (n1 <= n2) {
            if (accu <= limit) {
                accu *= n1;
            } else {
                result = result.multiply(BigDecimal.valueOf(accu));
                accu = n1;
            }
            n1++;
        }
        return result.multiply(BigDecimal.valueOf(accu));
    }

    private static BigDecimal factorialRecursion(final int n1, final int n2) {
        var threshold = n1 > 200 ? 80 : 150;
        if (n2 - n1 < threshold) {
            return factorialLoop(n1, n2);
        }
        final var mid = (n1 + n2) >> 1;
        return factorialRecursion(mid + 1, n2).multiply(factorialRecursion(n1, mid));
    }

    /**
     * Calculates the factorial of the specified {@link BigDecimal}.
     *
     * <p>This implementation uses
     * <a href="https://en.wikipedia.org/wiki/Spouge%27s_approximation">Spouge's approximation</a>
     * to calculate the factorial for non-integer values.</p>
     *
     * <p>This involves calculating a series of constants that depend on the desired precision.
     * Since this constant calculation is quite expensive (especially for higher precisions),
     * the constants for a specific precision will be cached
     * and subsequent calls to this method with the same precision will be much faster.</p>
     *
     * <p>It is therefore recommended to do one call to this method with the standard precision of your application during the startup phase
     * and to avoid calling it with many different precisions.</p>
     *
     * <p>See: <a href="https://en.wikipedia.org/wiki/Factorial#Extension_of_factorial_to_non-integer_values_of_argument">Wikipedia: Factorial - Extension of factorial to non-integer values of argument</a></p>
     *
     * @param x           the {@link BigDecimal}
     * @param mathContext the {@link MathContext} used for the result
     * @return the factorial {@link BigDecimal}
     * @throws ArithmeticException           if x is a negative integer value (-1, -2, -3, ...)
     * @throws UnsupportedOperationException if x is a non-integer value and the {@link MathContext} has unlimited precision
     * @see #factorial(int)
     */
    public static BigDecimal factorial(BigDecimal x, MathContext mathContext) {
        if (isIntValue(x)) {
            return round(factorial(x.intValueExact()), mathContext);
        }

        // https://en.wikipedia.org/wiki/Spouge%27s_approximation
        checkMathContext(mathContext);
        var mc = new MathContext(mathContext.getPrecision() * 2, mathContext.getRoundingMode());

        var a = mathContext.getPrecision() * 13 / 10;
        var constants = getSpougeFactorialConstants(a);

        var bigA = BigDecimal.valueOf(a);

        var negative = false;
        var factor = constants.get(0);
        for (var k = 1; k < a; k++) {
            var bigK = BigDecimal.valueOf(k);
            factor = factor.add(constants.get(k).divide(x.add(bigK), mc), mc);
            negative = !negative;
        }

        var result = pow(x.add(bigA, mc), x.add(BigDecimal.valueOf(0.5), mc), mc);
        result = result.multiply(exp(x.negate().subtract(bigA, mc), mc), mc);
        result = result.multiply(factor, mc);

        return round(result, mathContext);
    }

    static List<BigDecimal> getSpougeFactorialConstants(int a) {
        synchronized (spougeFactorialConstantsCacheLock) {
            return spougeFactorialConstantsCache.computeIfAbsent(a, key -> {
                List<BigDecimal> constants = new ArrayList<>(a);
                var mc = new MathContext(a * 15 / 10);

                var c0 = sqrt(pi(mc).multiply(TWO, mc), mc);
                constants.add(c0);

                var negative = false;
                for (var k = 1; k < a; k++) {
                    var bigK = BigDecimal.valueOf(k);
                    var ck = pow(BigDecimal.valueOf(a - k), bigK.subtract(ONE_HALF, mc), mc);
                    ck = ck.multiply(exp(BigDecimal.valueOf(a - k), mc), mc);
                    ck = ck.divide(factorial(k - 1), mc);
                    if (negative) {
                        ck = ck.negate();
                    }
                    constants.add(ck);

                    negative = !negative;
                }

                return Collections.unmodifiableList(constants);
            });
        }
    }

    /**
     * Calculates {@link BigDecimal} x to the power of {@link BigDecimal} y (x<sup>y</sup>).
     *
     * @param x           the {@link BigDecimal} value to take to the power
     * @param y           the {@link BigDecimal} value to serve as exponent
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated x to the power of y with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     * @see #pow(BigDecimal, long, MathContext)
     */
    public static BigDecimal pow(BigDecimal x, BigDecimal y, MathContext mathContext) {
        checkMathContext(mathContext);
        if (x.signum() == 0) {
            switch (y.signum()) {
                case 0:
                    return round(ONE, mathContext);
                case 1:
                    return round(ZERO, mathContext);
            }
        }

        // TODO optimize y=0, y=1, y=10^k, y=-1, y=-10^k

        try {
            var longValue = y.longValueExact();
            return pow(x, longValue, mathContext);
        } catch (ArithmeticException ignored) { /* Nothing */ }

        if (fractionalPart(y).signum() == 0) {
            return powInteger(x, y, mathContext);
        }

        // x^y = exp(y*log(x))
        var mc = new MathContext(mathContext.getPrecision() + 6, mathContext.getRoundingMode());
        var result = exp(y.multiply(log(x, mc), mc), mc);

        return round(result, mathContext);
    }

    /**
     * Calculates {@link BigDecimal} x to the power of <code>long</code> y (x<sup>y</sup>).
     *
     * <p>The implementation tries to minimize the number of multiplications of {@link BigDecimal x} (using squares whenever possible).</p>
     *
     * <p>See: <a href="https://en.wikipedia.org/wiki/Exponentiation#Efficient_computation_with_integer_exponents">Wikipedia: Exponentiation - efficient computation</a></p>
     *
     * @param x           the {@link BigDecimal} value to take to the power
     * @param y           the <code>long</code> value to serve as exponent
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated x to the power of y with the precision specified in the <code>mathContext</code>
     * @throws ArithmeticException if y is negative and the result is inexact but the
     *                             rounding mode is {@code UNNECESSARY} or
     *                             {@code mc.precision == 0} and the quotient has a
     *                             non-terminating decimal expansion.
     * @throws ArithmeticException if the rounding mode is
     *                             {@code UNNECESSARY} and the
     *                             {@code BigDecimal}  operation would require rounding.
     */
    public static BigDecimal pow(BigDecimal x, long y, MathContext mathContext) {
        var mc = mathContext.getPrecision() == 0
                ? mathContext
                : new MathContext(mathContext.getPrecision() + 10, mathContext.getRoundingMode());

        // TODO optimize y=0, y=1, y=10^k, y=-1, y=-10^k

        if (y < 0) {
            var value = reciprocal(pow(x, -y, mc), mc);
            return round(value, mathContext);
        }

        var result = ONE;
        while (y > 0) {
            if ((y & 1) == 1) {
                // odd exponent -> multiply result with x
                result = result.multiply(x, mc);
                y -= 1;
            }

            if (y > 0) {
                // even exponent -> square x
                x = x.multiply(x, mc);
            }

            y >>= 1;
        }

        return round(result, mathContext);
    }

    /**
     * Calculates {@link BigDecimal} x to the power of the integer value y (x<sup>y</sup>).
     *
     * <p>The value y MUST be an integer value.</p>
     *
     * @param x           the {@link BigDecimal} value to take to the power
     * @param integerY    the {@link BigDecimal} <strong>integer</strong> value to serve as exponent
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated x to the power of y with the precision specified in the <code>mathContext</code>
     * @see #pow(BigDecimal, long, MathContext)
     */
    private static BigDecimal powInteger(BigDecimal x, BigDecimal integerY, MathContext mathContext) {
        if (fractionalPart(integerY).signum() != 0) {
            throw new IllegalArgumentException("Not integer value: " + integerY);
        }

        if (integerY.signum() < 0) {
            return ONE.divide(powInteger(x, integerY.negate(), mathContext), mathContext);
        }

        var mc = new MathContext(Math.max(mathContext.getPrecision(), -integerY.scale()) + 30,
                mathContext.getRoundingMode()
        );

        var result = ONE;
        while (integerY.signum() > 0) {
            var halfY = integerY.divide(TWO, mc);

            if (fractionalPart(halfY).signum() != 0) {
                // odd exponent -> multiply result with x
                result = result.multiply(x, mc);
                integerY = integerY.subtract(ONE);
                halfY = integerY.divide(TWO, mc);
            }

            if (halfY.signum() > 0) {
                // even exponent -> square x
                x = x.multiply(x, mc);
            }

            integerY = halfY;
        }

        return round(result, mathContext);
    }

    /**
     * Calculates the square root of {@link BigDecimal} x.
     *
     * <p>See <a href="http://en.wikipedia.org/wiki/Square_root">Wikipedia: Square root</a></p>
     *
     * @param x           the {@link BigDecimal} value to calculate the square root
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated square root of x with the precision specified in the <code>mathContext</code>
     * @throws ArithmeticException           if x &lt; 0
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal sqrt(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        switch (x.signum()) {
            case 0:
                return ZERO;
            case -1:
                throw new ArithmeticException("Illegal sqrt(x) for x < 0: x = " + x);
        }

        var maxPrecision = mathContext.getPrecision() + 6;
        var acceptableError = ONE.movePointLeft(mathContext.getPrecision() + 1);

        BigDecimal result;
        int adaptivePrecision;
        if (isDoubleValue(x)) {
            result = BigDecimal.valueOf(Math.sqrt(x.doubleValue()));
            adaptivePrecision = EXPECTED_INITIAL_PRECISION;
        } else {
            result = x.multiply(ONE_HALF, mathContext);
            adaptivePrecision = 1;
        }

        BigDecimal last;

        if (adaptivePrecision < maxPrecision) {
            if (result.multiply(result).compareTo(x) == 0) {
                return round(result, mathContext); // early exit if x is a square number
            }

            do {
                last = result;
                adaptivePrecision = adaptivePrecision * 2;
                if (adaptivePrecision > maxPrecision) {
                    adaptivePrecision = maxPrecision;
                }
                var mc = new MathContext(adaptivePrecision, mathContext.getRoundingMode());
                result = x.divide(result, mc).add(last, mc).multiply(ONE_HALF, mc);
            }
            while (adaptivePrecision < maxPrecision || result.subtract(last).abs().compareTo(acceptableError) > 0);
        }

        return round(result, mathContext);
    }

    /**
     * Calculates the natural logarithm of {@link BigDecimal} x.
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Natural_logarithm">Wikipedia: Natural logarithm</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the natural logarithm for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated natural logarithm {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws ArithmeticException           if x &lt;= 0
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal log(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        if (x.signum() <= 0) {
            throw new ArithmeticException("Illegal log(x) for x <= 0: x = " + x);
        }
        if (x.compareTo(ONE) == 0) {
            return ZERO;
        }

        BigDecimal result;
        switch (x.compareTo(TEN)) {
            case 0:
                result = logTen(mathContext);
                break;
            case 1:
                result = logUsingExponent(x, mathContext);
                break;
            default:
                result = logUsingTwoThree(x, mathContext);
        }

        return round(result, mathContext);
    }

    /**
     * Calculates the logarithm of {@link BigDecimal} x to the base 2.
     *
     * @param x           the {@link BigDecimal} to calculate the logarithm base 2 for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated natural logarithm {@link BigDecimal} to the base 2 with the precision specified in the <code>mathContext</code>
     * @throws ArithmeticException           if x &lt;= 0
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal log2(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        var mc = new MathContext(mathContext.getPrecision() + 4, mathContext.getRoundingMode());

        var result = log(x, mc).divide(logTwo(mc), mc);
        return round(result, mathContext);
    }

    /**
     * Calculates the logarithm of {@link BigDecimal} x to the base 10.
     *
     * @param x           the {@link BigDecimal} to calculate the logarithm base 10 for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated natural logarithm {@link BigDecimal} to the base 10 with the precision specified in the <code>mathContext</code>
     * @throws ArithmeticException           if x &lt;= 0
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal log10(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        var mc = new MathContext(mathContext.getPrecision() + 2, mathContext.getRoundingMode());

        var result = log(x, mc).divide(logTen(mc), mc);
        return round(result, mathContext);
    }

    private static BigDecimal logUsingNewton(BigDecimal x, MathContext mathContext) {
        // https://en.wikipedia.org/wiki/Natural_logarithm in chapter 'High Precision'
        // y = y + 2 * (x-exp(y)) / (x+exp(y))

        var maxPrecision = mathContext.getPrecision() + 20;
        var acceptableError = ONE.movePointLeft(mathContext.getPrecision() + 1);
        //System.out.println("logUsingNewton(" + x + " " + mathContext + ") precision " + maxPrecision);

        BigDecimal result;
        int adaptivePrecision;
        var doubleX = x.doubleValue();
        if (doubleX > 0.0 && isDoubleValue(x)) {
            result = BigDecimal.valueOf(Math.log(doubleX));
            adaptivePrecision = EXPECTED_INITIAL_PRECISION;
        } else {
            result = x.divide(TWO, mathContext);
            adaptivePrecision = 1;
        }

        BigDecimal step;

        do {
            adaptivePrecision = adaptivePrecision * 3;
            if (adaptivePrecision > maxPrecision) {
                adaptivePrecision = maxPrecision;
            }
            var mc = new MathContext(adaptivePrecision, mathContext.getRoundingMode());

            var expY = BigDecimalMath.exp(result, mc);
            step = TWO.multiply(x.subtract(expY, mc), mc).divide(x.add(expY, mc), mc);
            //System.out.println("  step " + step + " adaptivePrecision=" + adaptivePrecision);
            result = result.add(step);
        }
        while (adaptivePrecision < maxPrecision || step.abs().compareTo(acceptableError) > 0);

        return result;
    }

    private static BigDecimal logUsingExponent(BigDecimal x, MathContext mathContext) {
        var mcDouble = new MathContext(mathContext.getPrecision() * 2, mathContext.getRoundingMode());
        var mc = new MathContext(mathContext.getPrecision() + 4, mathContext.getRoundingMode());
        //System.out.println("logUsingExponent(" + x + " " + mathContext + ") precision " + mc);

        var exponent = exponent(x);
        var mantissa = mantissa(x);

        var result = logUsingTwoThree(mantissa, mc);
        if (exponent != 0) {
            result = result.add(valueOf(exponent).multiply(logTen(mcDouble), mc), mc);
        }
        return result;
    }

    private static BigDecimal logUsingTwoThree(BigDecimal x, MathContext mathContext) {
        var mcDouble = new MathContext(mathContext.getPrecision() * 2, mathContext.getRoundingMode());
        var mc = new MathContext(mathContext.getPrecision() + 4, mathContext.getRoundingMode());
        //System.out.println("logUsingTwoThree(" + x + " " + mathContext + ") precision " + mc);

        var factorOfTwo = 0;
        var powerOfTwo = 1;
        var factorOfThree = 0;
        var powerOfThree = 1;

        var value = x.doubleValue();
        if (value < 0.01) {
            // do nothing
        } else if (value < 0.1) { // never happens when called by logUsingExponent()
            while (value < 0.6) {
                value *= 2;
                factorOfTwo--;
                powerOfTwo *= 2;
            }
        } else if (value < 0.115) { // (0.1 - 0.11111 - 0.115) -> (0.9 - 1.0 - 1.035)
            factorOfThree = -2;
            powerOfThree = 9;
        } else if (value < 0.14) { // (0.115 - 0.125 - 0.14) -> (0.92 - 1.0 - 1.12)
            factorOfTwo = -3;
            powerOfTwo = 8;
        } else if (value < 0.2) { // (0.14 - 0.16667 - 0.2) - (0.84 - 1.0 - 1.2)
            factorOfTwo = -1;
            powerOfTwo = 2;
            factorOfThree = -1;
            powerOfThree = 3;
        } else if (value < 0.3) { // (0.2 - 0.25 - 0.3) -> (0.8 - 1.0 - 1.2)
            factorOfTwo = -2;
            powerOfTwo = 4;
        } else if (value < 0.42) { // (0.3 - 0.33333 - 0.42) -> (0.9 - 1.0 - 1.26)
            factorOfThree = -1;
            powerOfThree = 3;
        } else if (value < 0.7) { // (0.42 - 0.5 - 0.7) -> (0.84 - 1.0 - 1.4)
            factorOfTwo = -1;
            powerOfTwo = 2;
        } else if (value < 1.4) { // (0.7 - 1.0 - 1.4) -> (0.7 - 1.0 - 1.4)
            // do nothing
        } else if (value < 2.5) { // (1.4 - 2.0 - 2.5) -> (0.7 - 1.0 - 1.25)
            factorOfTwo = 1;
            powerOfTwo = 2;
        } else if (value < 3.5) { // (2.5 - 3.0 - 3.5) -> (0.833333 - 1.0 - 1.166667)
            factorOfThree = 1;
            powerOfThree = 3;
        } else if (value < 5.0) { // (3.5 - 4.0 - 5.0) -> (0.875 - 1.0 - 1.25)
            factorOfTwo = 2;
            powerOfTwo = 4;
        } else if (value < 7.0) { // (5.0 - 6.0 - 7.0) -> (0.833333 - 1.0 - 1.166667)
            factorOfThree = 1;
            powerOfThree = 3;
            factorOfTwo = 1;
            powerOfTwo = 2;
        } else if (value < 8.5) { // (7.0 - 8.0 - 8.5) -> (0.875 - 1.0 - 1.0625)
            factorOfTwo = 3;
            powerOfTwo = 8;
        } else if (value < 10.0) { // (8.5 - 9.0 - 10.0) -> (0.94444 - 1.0 - 1.11111)
            factorOfThree = 2;
            powerOfThree = 9;
        } else {
            while (value > 1.4) { // never happens when called by logUsingExponent()
                value /= 2;
                factorOfTwo++;
                powerOfTwo *= 2;
            }
        }

        var correctedX = x;
        var result = ZERO;

        if (factorOfTwo > 0) {
            correctedX = correctedX.divide(valueOf(powerOfTwo), mc);
            result = result.add(logTwo(mcDouble).multiply(valueOf(factorOfTwo), mc), mc);
        } else if (factorOfTwo < 0) {
            correctedX = correctedX.multiply(valueOf(powerOfTwo), mc);
            result = result.subtract(logTwo(mcDouble).multiply(valueOf(-factorOfTwo), mc), mc);
        }

        if (factorOfThree > 0) {
            correctedX = correctedX.divide(valueOf(powerOfThree), mc);
            result = result.add(logThree(mcDouble).multiply(valueOf(factorOfThree), mc), mc);
        } else if (factorOfThree < 0) {
            correctedX = correctedX.multiply(valueOf(powerOfThree), mc);
            result = result.subtract(logThree(mcDouble).multiply(valueOf(-factorOfThree), mc), mc);
        }

        if (x.equals(correctedX) && result.equals(ZERO)) {
            return logUsingNewton(x, mathContext);
        }

        result = result.add(logUsingNewton(correctedX, mc), mc);

        return result;
    }

    /**
     * Returns the number e.
     *
     * <p>See <a href="https://en.wikipedia.org/wiki/E_(mathematical_constant)">Wikipedia: E (mathematical_constant)</a></p>
     *
     * @param mathContext the {@link MathContext} used for the result
     * @return the number e with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal e(MathContext mathContext) {
        checkMathContext(mathContext);
        BigDecimal result;

        synchronized (eCacheLock) {
            if (eCache != null && mathContext.getPrecision() <= eCache.precision()) {
                result = eCache;
            } else {
                eCache = exp(ONE, mathContext);
                return eCache;
            }
        }

        return round(result, mathContext);
    }

    /**
     * Returns the number pi.
     *
     * <p>See <a href="https://en.wikipedia.org/wiki/Pi">Wikipedia: Pi</a></p>
     *
     * @param mathContext the {@link MathContext} used for the result
     * @return the number pi with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal pi(MathContext mathContext) {
        checkMathContext(mathContext);
        BigDecimal result;

        synchronized (piCacheLock) {
            if (piCache != null && mathContext.getPrecision() <= piCache.precision()) {
                result = piCache;
            } else {
                piCache = piChudnovski(mathContext);
                return piCache;
            }
        }

        return round(result, mathContext);
    }

    private static BigDecimal piChudnovski(MathContext mathContext) {
        var mc = new MathContext(mathContext.getPrecision() + 10, mathContext.getRoundingMode());

        final var value24 = BigDecimal.valueOf(24);
        final var value640320 = BigDecimal.valueOf(640320);
        final var value13591409 = BigDecimal.valueOf(13591409);
        final var value545140134 = BigDecimal.valueOf(545140134);
        final var valueDivisor = value640320.pow(3).divide(value24, mc);

        var sumA = BigDecimal.ONE;
        var sumB = BigDecimal.ZERO;

        var a = BigDecimal.ONE;
        long dividendTerm1 = 5; // -(6*k - 5)
        long dividendTerm2 = -1; // 2*k - 1
        long dividendTerm3 = -1; // 6*k - 1
        BigDecimal kPower3;

        long iterationCount = (mc.getPrecision() + 13) / 14;
        for (long k = 1; k <= iterationCount; k++) {
            var valueK = BigDecimal.valueOf(k);
            dividendTerm1 += -6;
            dividendTerm2 += 2;
            dividendTerm3 += 6;
            var dividend = BigDecimal.valueOf(dividendTerm1).multiply(BigDecimal.valueOf(dividendTerm2))
                                            .multiply(BigDecimal.valueOf(dividendTerm3));
            kPower3 = valueK.pow(3);
            var divisor = kPower3.multiply(valueDivisor, mc);
            a = a.multiply(dividend).divide(divisor, mc);
            var b = valueK.multiply(a, mc);

            sumA = sumA.add(a);
            sumB = sumB.add(b);
        }

        final var value426880 = BigDecimal.valueOf(426880);
        final var value10005 = BigDecimal.valueOf(10005);
        final var factor = value426880.multiply(sqrt(value10005, mc));
        var pi = factor.divide(value13591409.multiply(sumA, mc).add(value545140134.multiply(sumB, mc)), mc);

        return round(pi, mathContext);
    }

    private static BigDecimal logTen(MathContext mathContext) {
        BigDecimal result;

        synchronized (log10CacheLock) {
            if (log10Cache != null && mathContext.getPrecision() <= log10Cache.precision()) {
                result = log10Cache;
            } else {
                log10Cache = logUsingNewton(BigDecimal.TEN, mathContext);
                return log10Cache;
            }
        }

        return round(result, mathContext);
    }

    private static BigDecimal logTwo(MathContext mathContext) {
        BigDecimal result;

        synchronized (log2CacheLock) {
            if (log2Cache != null && mathContext.getPrecision() <= log2Cache.precision()) {
                result = log2Cache;
            } else {
                log2Cache = logUsingNewton(TWO, mathContext);
                return log2Cache;
            }
        }

        return round(result, mathContext);
    }

    private static BigDecimal logThree(MathContext mathContext) {
        BigDecimal result;
        synchronized (log3CacheLock) {
            if (log3Cache != null && mathContext.getPrecision() <= log3Cache.precision()) {
                result = log3Cache;
            } else {
                log3Cache = logUsingNewton(THREE, mathContext);
                return log3Cache;
            }
        }

        return round(result, mathContext);
    }

    /**
     * Calculates the natural exponent of {@link BigDecimal} x (e<sup>x</sup>).
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Exponent">Wikipedia: Exponent</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the exponent for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated exponent {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal exp(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        if (x.signum() == 0) {
            return ONE;
        }

        return expIntegralFractional(x, mathContext);
    }

    private static BigDecimal expIntegralFractional(BigDecimal x, MathContext mathContext) {
        var integralPart = integralPart(x);

        if (integralPart.signum() == 0) {
            return expTaylor(x, mathContext);
        }

        var fractionalPart = x.subtract(integralPart);

        var mc = new MathContext(mathContext.getPrecision() + 10, mathContext.getRoundingMode());

        var z = ONE.add(fractionalPart.divide(integralPart, mc));
        var t = expTaylor(z, mc);

        var result = pow(t, integralPart.intValueExact(), mc);

        return round(result, mathContext);
    }

    private static BigDecimal expTaylor(BigDecimal x, MathContext mathContext) {
        var mc = new MathContext(mathContext.getPrecision() + 6, mathContext.getRoundingMode());

        x = x.divide(valueOf(256), mc);

        var result = ExpCalculator.INSTANCE.calculate(x, mc);
        result = BigDecimalMath.pow(result, 256, mc);
        return round(result, mathContext);
    }

    /**
     * Calculates the sine (sinus) of {@link BigDecimal} x.
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Sine">Wikipedia: Sine</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the sine for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated sine {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal sin(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        var mc = new MathContext(mathContext.getPrecision() + 6, mathContext.getRoundingMode());

        if (x.abs().compareTo(ROUGHLY_TWO_PI) > 0) {
            var mc2 = new MathContext(mc.getPrecision() + 4, mathContext.getRoundingMode());
            var twoPi = TWO.multiply(pi(mc2), mc2);
            x = x.remainder(twoPi, mc2);
        }

        var result = SinCalculator.INSTANCE.calculate(x, mc);
        return round(result, mathContext);
    }

    /**
     * Calculates the arc sine (inverted sine) of {@link BigDecimal} x.
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Arcsine">Wikipedia: Arcsine</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the arc sine for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated arc sine {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws ArithmeticException           if x &gt; 1 or x &lt; -1
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal asin(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        if (x.compareTo(ONE) > 0) {
            throw new ArithmeticException("Illegal asin(x) for x > 1: x = " + x);
        }
        if (x.compareTo(MINUS_ONE) < 0) {
            throw new ArithmeticException("Illegal asin(x) for x < -1: x = " + x);
        }

        if (x.signum() == -1) {
            return asin(x.negate(), mathContext).negate();
        }

        var mc = new MathContext(mathContext.getPrecision() + 6, mathContext.getRoundingMode());

        if (x.compareTo(BigDecimal.valueOf(0.707107)) >= 0) {
            var xTransformed = sqrt(ONE.subtract(x.multiply(x, mc), mc), mc);
            return acos(xTransformed, mathContext);
        }

        var result = AsinCalculator.INSTANCE.calculate(x, mc);
        return round(result, mathContext);
    }

    /**
     * Calculates the cosine (cosinus) of {@link BigDecimal} x.
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Cosine">Wikipedia: Cosine</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the cosine for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated cosine {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal cos(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        var mc = new MathContext(mathContext.getPrecision() + 6, mathContext.getRoundingMode());

        if (x.abs().compareTo(ROUGHLY_TWO_PI) > 0) {
            var mc2 = new MathContext(mc.getPrecision() + 4, mathContext.getRoundingMode());
            var twoPi = TWO.multiply(pi(mc2), mc2);
            x = x.remainder(twoPi, mc2);
        }

        var result = CosCalculator.INSTANCE.calculate(x, mc);
        return round(result, mathContext);
    }

    /**
     * Calculates the arc cosine (inverted cosine) of {@link BigDecimal} x.
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Arccosine">Wikipedia: Arccosine</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the arc cosine for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated arc sine {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws ArithmeticException           if x &gt; 1 or x &lt; -1
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal acos(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        if (x.compareTo(ONE) > 0) {
            throw new ArithmeticException("Illegal acos(x) for x > 1: x = " + x);
        }
        if (x.compareTo(MINUS_ONE) < 0) {
            throw new ArithmeticException("Illegal acos(x) for x < -1: x = " + x);
        }

        var mc = new MathContext(mathContext.getPrecision() + 6, mathContext.getRoundingMode());

        var result = pi(mc).divide(TWO, mc).subtract(asin(x, mc), mc);
        return round(result, mathContext);
    }

    /**
     * Calculates the tangens of {@link BigDecimal} x.
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Tangens">Wikipedia: Tangens</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the tangens for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated tangens {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal tan(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        if (x.signum() == 0) {
            return ZERO;
        }

        var mc = new MathContext(mathContext.getPrecision() + 4, mathContext.getRoundingMode());
        var result = sin(x, mc).divide(cos(x, mc), mc);
        return round(result, mathContext);
    }

    /**
     * Calculates the arc tangens (inverted tangens) of {@link BigDecimal} x.
     *
     * <p>See: <a href="http://en.wikipedia.org/wiki/Arctangens">Wikipedia: Arctangens</a></p>
     *
     * @param x           the {@link BigDecimal} to calculate the arc tangens for
     * @param mathContext the {@link MathContext} used for the result
     * @return the calculated arc tangens {@link BigDecimal} with the precision specified in the <code>mathContext</code>
     * @throws UnsupportedOperationException if the {@link MathContext} has unlimited precision
     */
    public static BigDecimal atan(BigDecimal x, MathContext mathContext) {
        checkMathContext(mathContext);
        var mc = new MathContext(mathContext.getPrecision() + 6, mathContext.getRoundingMode());

        x = x.divide(sqrt(ONE.add(x.multiply(x, mc), mc), mc), mc);

        var result = asin(x, mc);
        return round(result, mathContext);
    }

    private static void checkMathContext(MathContext mathContext) {
        if (mathContext.getPrecision() == 0) {
            throw new UnsupportedOperationException("Unlimited MathContext not supported");
        }
    }

    public static BigDecimal getBigDecimal(Number n) {
        return n instanceof BigDecimal
                ? (BigDecimal) n : n instanceof BigInteger
                ? new BigDecimal((BigInteger) n)
                : new BigDecimal(n.toString());
    }

    public static BigInteger getBigInteger(Number n) {
        return n instanceof BigInteger ? (BigInteger) n : BigInteger.valueOf(n.longValue());
    }
}
