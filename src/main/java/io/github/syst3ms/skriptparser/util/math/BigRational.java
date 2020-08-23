package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * A rational number represented as a quotient of two values.
 * 
 * <p>Basic calculations with rational numbers (+ - * /) have no loss of precision.
 * This allows to use {@link BigRational} as a replacement for {@link BigDecimal} if absolute accuracy is desired.</p>
 * 
 * <p><a href="http://en.wikipedia.org/wiki/Rational_number">Wikipedia: Rational number</a></p>
 * 
 * <p>The values are internally stored as {@link BigDecimal} (for performance optimizations) but represented
 * as {@link BigInteger} (for mathematical correctness)
 *
 * <p>The following basic calculations have no loss of precision:</p>
 * <ul>
 * <li>{@link #add(BigRational)}</li>
 * <li>{@link #subtract(BigRational)}</li>
 * <li>{@link #multiply(BigRational)}</li>
 * <li>{@link #divide(BigRational)}</li>
 * <li>{@link #pow(int)}</li>
 * </ul>
 * 
 * <p>The following calculations are special cases of the ones listed above and have no loss of precision:</p>
 * <ul>
 * <li>{@link #negate()}</li>
 * <li>{@link #reciprocal()}</li>
 * </ul>
 *
 * I do not claim ownership of this code, it is the intellectual property of <a href="github.com/eobermuhlner">@obermuhlner</a>.
 * @author @obermuhlner
 */
public class BigRational implements Comparable<BigRational> {

	/**
	 * The value 0 as {@link BigRational}.
	 */
	public static final BigRational ZERO = new BigRational(0);
	/**
	 * The value 1 as {@link BigRational}.
	 */
	public static final BigRational ONE = new BigRational(1);

    private final BigDecimal numerator;

	private final BigDecimal denominator;

	private BigRational(int value) {
		this(BigDecimal.valueOf(value), BigDecimal.ONE);
	}

	private BigRational(BigDecimal num, BigDecimal denom) {
		var n = num;
		var d = denom;

		if (d.signum() == 0) {
			throw new ArithmeticException("Divide by zero");
		}

		if (d.signum() < 0) {
			n = n.negate();
			d = d.negate();
		}

		numerator = n;
		denominator = d;
	}

    /**
	 * Returns the numerator of this rational number as BigDecimal.
	 * 
	 * @return the numerator as BigDecimal
	 */
	public BigDecimal getNumerator() {
		return numerator;
	}

    /**
	 * Returns the denominator of this rational number as BigDecimal.
	 * 
	 * <p>Guaranteed to not be 0.</p>
	 * <p>Guaranteed to be positive.</p>
	 * 
	 * @return the denominator as BigDecimal
	 */
	public BigDecimal getDenominator() {
		return denominator;
	}

    /**
	 * Negates this rational number (inverting the sign).
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * <p>Examples:</p>
	 * <ul>
	 * <li><code>BigRational.valueOf(3.5).negate()</code> returns <code>BigRational.valueOf(-3.5)</code></li>
	 * </ul>
	 * 
	 * @return the negated rational number
	 */
	public BigRational negate() {
		if (isZero()) {
			return this;
		}

		return of(numerator.negate(), denominator);
	}

	/**
	 * Calculates the reciprocal of this rational number (1/x).
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * <p>Examples:</p>
	 * <ul>
	 * <li><code>BigRational.valueOf(0.5).reciprocal()</code> returns <code>BigRational.valueOf(2)</code></li>
	 * <li><code>BigRational.valueOf(-2).reciprocal()</code> returns <code>BigRational.valueOf(-0.5)</code></li>
	 * </ul>
	 * 
	 * @return the reciprocal rational number
	 * @throws ArithmeticException if this number is 0 (division by zero)
	 */
	public BigRational reciprocal() {
		return of(denominator, numerator);
	}

    /**
	 * Calculates the addition (+) of this rational number and the specified argument.
	 *
	 * <p>The result has no loss of precision.</p>
	 *
	 * @param value the rational number to add
	 * @return the resulting rational number
	 */
	public BigRational add(BigRational value) {
		if (denominator.equals(value.denominator)) {
			return of(numerator.add(value.numerator), denominator);
		}

		var n = numerator.multiply(value.denominator).add(value.numerator.multiply(denominator));
		var d = denominator.multiply(value.denominator);
		return of(n, d);
	}

	private BigRational add(BigDecimal value) {
		return of(numerator.add(value.multiply(denominator)), denominator);
	}

	/**
	 * Calculates the addition (+) of this rational number and the specified argument.
	 *
	 * <p>This is functionally identical to
	 * <code>this.add(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 *
	 * <p>The result has no loss of precision.</p>
	 *
	 * @param value the {@link BigInteger} to add
	 * @return the resulting rational number
	 */
	public BigRational add(BigInteger value) {
		if (value.equals(BigInteger.ZERO)) {
			return this;
		}
		return add(new BigDecimal(value));
	}

    /**
	 * Calculates the subtraction (-) of this rational number and the specified argument.
	 *
	 * <p>The result has no loss of precision.</p>
	 *
	 * @param value the rational number to subtract
	 * @return the resulting rational number
	 */
	public BigRational subtract(BigRational value) {
		if (denominator.equals(value.denominator)) {
			return of(numerator.subtract(value.numerator), denominator);
		}

		var n = numerator.multiply(value.denominator).subtract(value.numerator.multiply(denominator));
		var d = denominator.multiply(value.denominator);
		return of(n, d);
	}

    /**
	 * Calculates the multiplication (*) of this rational number and the specified argument.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the rational number to multiply
	 * @return the resulting rational number
	 */
	public BigRational multiply(BigRational value) {
		if (isZero() || value.isZero()) {
			return ZERO;
		}
		if (equals(ONE)) {
			return value;
		}
		if (value.equals(ONE)) {
			return this;
		}

		return of(numerator.multiply(value.numerator), denominator.multiply(value.denominator));
	}

	// private, because we want to hide that we use BigDecimal internally
	private BigRational multiply(BigDecimal value) {
		return of(numerator.multiply(value), denominator);
	}
	
	/**
	 * Calculates the multiplication (*) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.multiply(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the {@link BigInteger} to multiply
	 * @return the resulting rational number
	 */
	public BigRational multiply(BigInteger value) {
		if (isZero() || value.signum() == 0) {
			return ZERO;
		}
		if (equals(ONE)) {
			return valueOf(value);
		}
		if (value.equals(BigInteger.ONE)) {
			return this;
		}

		return multiply(new BigDecimal(value));
	}

	/**
	 * Calculates the multiplication (*) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.multiply(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the int value to multiply
	 * @return the resulting rational number
	 */
	public BigRational multiply(int value) {
		return multiply(BigInteger.valueOf(value));
	}

	/**
	 * Calculates the division (/) of this rational number and the specified argument.
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the rational number to divide (0 is not allowed)
	 * @return the resulting rational number
	 * @throws ArithmeticException if the argument is 0 (division by zero)
	 */
	public BigRational divide(BigRational value) {
		if (value.equals(ONE)) {
			return this;
		}
		return of(numerator.multiply(value.denominator), denominator.multiply(value.numerator));
	}

	private BigRational divide(BigDecimal value) {
		return of(numerator, denominator.multiply(value));
	}
	
	/**
	 * Calculates the division (/) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.divide(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the {@link BigInteger} to divide (0 is not allowed)
	 * @return the resulting rational number
	 * @throws ArithmeticException if the argument is 0 (division by zero)
	 */
	public BigRational divide(BigInteger value) {
		if (value.equals(BigInteger.ONE)) {
			return this;
		}
		return divide(new BigDecimal(value));
	}

	/**
	 * Calculates the division (/) of this rational number and the specified argument.
	 * 
	 * <p>This is functionally identical to
	 * <code>this.divide(BigRational.valueOf(value))</code>
	 * but slightly faster.</p>
	 * 
	 * <p>The result has no loss of precision.</p>
	 * 
	 * @param value the int value to divide (0 is not allowed)
	 * @return the resulting rational number
	 * @throws ArithmeticException if the argument is 0 (division by zero)
	 */
	public BigRational divide(int value) {
		return divide(BigInteger.valueOf(value));
	}

	/**
	 * Returns whether this rational number is zero.
	 * 
	 * @return <code>true</code> if this rational number is zero (0), <code>false</code> if it is not zero
	 */
	public boolean isZero() {
		return numerator.signum() == 0;
	}

	/**
	 * Returns whether this rational number is an integer number without fraction part.
	 *
	 * <p>Will return <code>false</code> if this number is not reduced to the integer representation yet (e.g. 4/4 or 4/2)</p>
	 *
	 * @return <code>true</code> if this rational number is an integer number, <code>false</code> if it has a fraction part
	 */
	private boolean isIntegerInternal() {
		return denominator.compareTo(BigDecimal.ONE) == 0;
	}

	/**
	 * Calculates this rational number to the power (x<sup>y</sup>) of the specified argument.
	 *
	 * <p>The result has no loss of precision.</p>
	 *
	 * @param exponent exponent to which this rational number is to be raised
	 * @return the resulting rational number
	 */
	public BigRational pow(int exponent) {
		if (exponent == 0) {
			return ONE;
		}
		if (exponent == 1) {
			return this;
		}

		final BigInteger n;
		final BigInteger d;
		if (exponent > 0) {
			n = numerator.toBigInteger().pow(exponent);
			d = denominator.toBigInteger().pow(exponent);
		}
		else {
			n = denominator.toBigInteger().pow(-exponent);
			d = numerator.toBigInteger().pow(-exponent);
		}
		return valueOf(n, d);
	}

    private static int countDigits(BigInteger number) {
		var factor = Math.log(2) / Math.log(10);
		var digitCount = (int) (factor * number.bitLength() + 1);
		if (BigInteger.TEN.pow(digitCount - 1).compareTo(number) > 0) {
			return digitCount - 1;
		}
		return digitCount;
	}

	// TODO what is precision of a rational?
	private int precision() {
		return countDigits(numerator.toBigInteger()) + countDigits(denominator.toBigInteger());
	}

    /**
	 * Returns this rational number as a {@link BigDecimal}.
	 *
	 * @return the {@link BigDecimal} value
	 */
	public BigDecimal toBigDecimal() {
		var precision = Math.max(precision(), MathContext.DECIMAL128.getPrecision());
		return toBigDecimal(new MathContext(precision));
	}

	/**
	 * Returns this rational number as a {@link BigDecimal} with the precision specified by the {@link MathContext}.
	 *
	 * @param mc the {@link MathContext} specifying the precision of the calculated result
	 * @return the {@link BigDecimal}
	 */
	public BigDecimal toBigDecimal(MathContext mc) {
		return numerator.divide(denominator, mc);
	}

	@Override
	public int compareTo(BigRational other) {
		if (this == other) {
			return 0;
		}
		return numerator.multiply(other.denominator).compareTo(denominator.multiply(other.numerator));
	}

	@Override
	public int hashCode() {
		if (isZero()) {
			return 0;
		}
		return numerator.hashCode() + denominator.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof BigRational)) {
			return false;
		}

		var other = (BigRational) obj;
		if (!numerator.equals(other.numerator)) {
			return false;
		}
		return denominator.equals(other.denominator);
	}

	@Override
	public String toString() {
		if (isZero()) {
			return "0";
		}
		if (isIntegerInternal()) {
			return numerator.toString();
		}
		return toBigDecimal().toString();
	}

    /**
	 * Creates a rational number of the specified int value.
	 * 
	 * @param value the int value
	 * @return the rational number
	 */
	public static BigRational valueOf(int value) {
		if (value == 0) {
			return ZERO;
		}
		if (value == 1) {
			return ONE;
		}
		return new BigRational(value);
	}

	/**
	 * Creates a rational number of the specified numerator/denominator int values.
	 *
	 * @param numerator the numerator int value
	 * @param denominator the denominator int value (0 not allowed)
	 * @return the rational number
	 * @throws ArithmeticException if the denominator is 0 (division by zero)
	 */
	public static BigRational valueOf(int numerator, int denominator) {
		return of(BigDecimal.valueOf(numerator), BigDecimal.valueOf(denominator));
	}

    /**
	 * Creates a rational number of the specified numerator/denominator BigInteger values.
	 *
	 * @param numerator the numerator {@link BigInteger} value
	 * @param denominator the denominator {@link BigInteger} value (0 not allowed)
	 * @return the rational number
	 * @throws ArithmeticException if the denominator is 0 (division by zero)
	 */
	public static BigRational valueOf(BigInteger numerator, BigInteger denominator) {
		return of(new BigDecimal(numerator), new BigDecimal(denominator));
	}

	/**
	 * Creates a rational number of the specified {@link BigInteger} value.
	 * 
	 * @param value the {@link BigInteger} value
	 * @return the rational number
	 */
	public static BigRational valueOf(BigInteger value) {
		if (value.compareTo(BigInteger.ZERO) == 0) {
			return ZERO;
		}
		if (value.compareTo(BigInteger.ONE) == 0) {
			return ONE;
		}
		return valueOf(value, BigInteger.ONE);
	}

    /**
	 * Creates a rational number of the specified {@link BigDecimal} value.
	 *
	 * @param value the double value
	 * @return the rational number
	 */
	public static BigRational valueOf(BigDecimal value) {
		if (value.compareTo(BigDecimal.ZERO) == 0) {
			return ZERO;
		}
		if (value.compareTo(BigDecimal.ONE) == 0) {
			return ONE;
		}

		var scale = value.scale();
		if (scale == 0) {
			return new BigRational(value, BigDecimal.ONE);
		} else if (scale < 0) {
			var n = new BigDecimal(value.unscaledValue()).multiply(BigDecimal.ONE.movePointLeft(value.scale()));
			return new BigRational(n, BigDecimal.ONE);
		}
		else {
			var n = new BigDecimal(value.unscaledValue());
			var d = BigDecimal.ONE.movePointRight(value.scale());
			return new BigRational(n, d);
		}
	}

    private static BigRational of(BigDecimal numerator, BigDecimal denominator) {
		if (numerator.signum() == 0 && denominator.signum() != 0) {
			return ZERO;
		}
		if (numerator.compareTo(BigDecimal.ONE) == 0 && denominator.compareTo(BigDecimal.ONE) == 0) {
			return ONE;
		}
		return new BigRational(numerator, denominator);
	}

}
