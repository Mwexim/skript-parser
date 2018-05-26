package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Vector;

/**
 * Fractions (rational numbers).
 * They are ratios of two BigInteger numbers, reduced to coprime
 * numerator and denominator.
 *
 * @author Richard J. Mathar
 * @since 2006-06-25
 */
public class Rational implements Cloneable, Comparable<Rational> {
	/**
	 * The constant 0.
	 */
	public static Rational ZERO = new Rational();
	/**
	 * The constant 1.
	 */
	static Rational ONE = new Rational(1, 1);
	/**
	 * numerator
	 */
	private BigInteger a;
	/**
	 * denominator, always larger than zero.
	 */
	private BigInteger b;

	/**
	 * Default ctor, which represents the zero.
	 *
	 * @author Richard J. Mathar
	 * @since 2007-11-17
	 */
	public Rational() {
		a = BigInteger.ZERO;
		b = BigInteger.ONE;
	}

	/**
	 * ctor from a numerator and denominator.
	 *
	 * @param a the numerator.
	 * @param b the denominator.
	 * @author Richard J. Mathar
	 */
	public Rational(BigInteger a, BigInteger b) {
		this.a = a;
		this.b = b;
		normalize();
	}

	/**
	 * ctor from a numerator and denominator.
	 *
	 * @param a the numerator.
	 * @param b the denominator.
	 * @author Richard J. Mathar
	 */
	public Rational(int a, int b) {
		this(new BigInteger(String.valueOf(a)), new BigInteger(String.valueOf(b)));
	}

	/**
	 * ctor from a terminating continued fraction.
	 * Constructs the value of cfr[0]+1/(cfr[1]+1/(cfr[2]+...))).
	 *
	 * @param cfr The coefficients cfr[0], cfr[1],... of the continued fraction.
	 *            An exception is thrown if any of these is zero.
	 * @author Richard J. Mathar
	 * @since 2012-03-08
	 */
    private Rational(Vector<BigInteger> cfr) {
		if (cfr.isEmpty()) {
			throw new NumberFormatException("Empty continued fraction");
		} else if (cfr.size() == 1) {
			this.a = cfr.firstElement();
			this.b = BigInteger.ONE;
		} else {
			Vector<BigInteger> clond = new Vector<>();
			for (int i = 1; i < cfr.size(); i++)
				clond.add(cfr.elementAt(i));
			Rational rec = new Rational(clond);
			this.a = cfr.firstElement().multiply(rec.a).add(rec.b);
			this.b = rec.a;
			normalize();
		}
	}

	/**
	 * Create a copy.
	 *
	 * @author Richard J. Mathar
	 * @since 2008-11-07
	 */
	public Rational clone() {
		BigInteger aclon = new BigInteger(String.valueOf(a));
		BigInteger bclon = new BigInteger(String.valueOf(b));
		return new Rational(aclon, bclon);
	}

	/**
	 * Multiply by another fraction.
	 *
	 * @param val a second rational number.
	 * @return the product of this with the val.
	 * @author Richard J. Mathar
	 */
	public Rational multiply(final Rational val) {
		BigInteger num = a.multiply(val.a);
		BigInteger deno = b.multiply(val.b);
		return (new Rational(num, deno));
	}

	/**
	 * Multiply by a BigInteger.
	 *
	 * @param val a second number.
	 * @return the product of this with the value.
	 * @author Richard J. Mathar
	 */
	public Rational multiply(final BigInteger val) {
		Rational val2 = new Rational(val, BigInteger.ONE);
		return (multiply(val2));
	}

	/**
	 * Divide by another fraction.
	 *
	 * @param val A second rational number.
	 * @return The value of this/val
	 * @author Richard J. Mathar
	 */
	public Rational divide(final Rational val) {
		if (val.compareTo(Rational.ZERO) == 0) {
			throw new ArithmeticException("Dividing " + toString() + " through zero.");
		}
		BigInteger num = a.multiply(val.b);
		BigInteger deno = b.multiply(val.a);
		return (new Rational(num, deno));
	}

	/**
	 * Divide by an integer.
	 *
	 * @param val a second number.
	 * @return the value of this/val
	 * @author Richard J. Mathar
	 */
	public Rational divide(BigInteger val) {
		if (val.compareTo(BigInteger.ZERO) == 0) {
			throw new ArithmeticException("Dividing " + toString() + " through zero.");
		}
		Rational val2 = new Rational(val, BigInteger.ONE);
		return (divide(val2));
	}

	/**
	 * Divide by an integer.
	 *
	 * @param val A second number.
	 * @return The value of this/val
	 * @author Richard J. Mathar
	 */
	public Rational divide(int val) {
		if (val == 0) {
			throw new ArithmeticException("Dividing " + toString() + " through zero.");
		}
		Rational val2 = new Rational(val, 1);
		return (divide(val2));
	}

	/**
	 * Add another fraction.
	 *
	 * @param val The number to be added
	 * @return this+val.
	 * @author Richard J. Mathar
	 */
	public Rational add(Rational val) {
		BigInteger num = a.multiply(val.b).add(b.multiply(val.a));
		BigInteger deno = b.multiply(val.b);
		return (new Rational(num, deno));
	}

	/**
	 * Add another integer.
	 *
	 * @param val The number to be added
	 * @return this+val.
	 * @author Richard J. Mathar
	 */
	public Rational add(BigInteger val) {
		Rational val2 = new Rational(val, BigInteger.ONE);
		return (add(val2));
	}

	/**
	 * Add another integer.
	 *
	 * @param val The number to be added
	 * @return this+val.
	 * @author Richard J. Mathar
	 * @since May 26 2010
	 */
	public Rational add(int val) {
		BigInteger val2 = a.add(b.multiply(new BigInteger(String.valueOf(val))));
		return new Rational(val2, b);
	}

	/**
	 * Compute the negative.
	 *
	 * @return -this.
	 * @author Richard J. Mathar
	 */
	public Rational negate() {
		return (new Rational(a.negate(), b));
	}

	/**
	 * Subtract another fraction.
	 *
	 * @param val the number to be subtracted from this
	 * @return this - val.
	 * @author Richard J. Mathar
	 */
	public Rational subtract(Rational val) {
		Rational val2 = val.negate();
		return (add(val2));
	}

	/**
	 * Subtract an integer.
	 *
	 * @param val the number to be subtracted from this
	 * @return this - val.
	 * @author Richard J. Mathar
	 */
	public Rational subtract(BigInteger val) {
		Rational val2 = new Rational(val, BigInteger.ONE);
		return (subtract(val2));
	}

	/**
	 * Absolute value.
	 *
	 * @return The absolute (non-negative) value of this.
	 * @author Richard J. Mathar
	 */
	public Rational abs() {
		return (new Rational(a.abs(), b.abs()));
	}

	/**
	 * Compares the value of this with another constant.
	 *
	 * @param val the other constant to compare with
	 * @return -1, 0 or 1 if this number is numerically less than, equal to,
	 * or greater than val.
	 * @author Richard J. Mathar
	 */
	public int compareTo(final Rational val) {
		final BigInteger left = a.multiply(val.b);
		final BigInteger right = val.a.multiply(b);
		return left.compareTo(right);
	}

	/**
	 * Compares the value of this with another constant.
	 *
	 * @param val the other constant to compare with
	 * @return -1, 0 or 1 if this number is numerically less than, equal to,
	 * or greater than val.
	 * @author Richard J. Mathar
	 */
	public int compareTo(final BigInteger val) {
		final Rational val2 = new Rational(val, BigInteger.ONE);
		return (compareTo(val2));
	}

	/**
	 * Return a string in the format number/denom.
	 * If the denominator equals 1, print just the numerator without a slash.
	 *
	 * @return the human-readable version in base 10
	 * @author Richard J. Mathar
	 */
	public String toString() {
		if (b.compareTo(BigInteger.ONE) != 0) {
			return (a.toString() + "/" + b.toString());
		} else {
			return a.toString();
		}
	}

	/**
	 * Return a double value representation.
	 *
	 * @return The value with double precision.
	 * @author Richard J. Mathar
	 * @since 2008-10-26
	 */
	public double doubleValue() {
		BigDecimal adivb = (new BigDecimal(a)).divide(new BigDecimal(b), MathContext.DECIMAL128);
		return adivb.doubleValue();
	}

	/**
	 * Return a representation as BigDecimal.
	 *
	 * @param mc the mathematical context which determines precision, rounding mode etc
	 * @return A representation as a BigDecimal floating point number.
	 * @author Richard J. Mathar
	 * @since 2008-10-26
	 */
	public BigDecimal BigDecimalValue(MathContext mc) {
		BigDecimal n = new BigDecimal(a);
		BigDecimal d = new BigDecimal(b);
		return BigDecimalMath.scalePrec(n.divide(d, mc), mc);
	}

	/**
	 * Normalize to coprime numerator and denominator.
	 * Also copy a negative sign of the denominator to the numerator.
	 *
	 * @author Richard J. Mathar
	 * @since 2008-10-19
	 */
    private void normalize() {
		final BigInteger g = a.gcd(b);
		if (g.compareTo(BigInteger.ONE) > 0) {
			a = a.divide(g);
			b = b.divide(g);
		}
		if (b.compareTo(BigInteger.ZERO) < 0) {
			a = a.negate();
			b = b.negate();
		}
	}
}
