package io.github.syst3ms.skriptparser.util.math;

import java.math.BigInteger;
import java.util.Vector;


/**
 * Bernoulli numbers.
 *
 * @author Richard J. Mathar
 * @since 2006-06-25
 */
class Bernoulli {
	private static Vector<Rational> a = new Vector<>();

	static {
		a.add(Rational.ONE);
		a.add(new Rational(1, 6));
	}

	/**
	 * Set a coefficient in the internal table.
	 *
	 * @param n     the zero-based index of the coefficient. n=0 for the constant term.
	 * @param value the new value of the coefficient.
	 * @author Richard J. Mathar
	 */
	private void set(final int n, final Rational value) {
		final int nindx = n / 2;
		if (nindx < a.size()) {
			a.set(nindx, value);
		} else {
			while (a.size() < nindx) a.add(Rational.ZERO);
			a.add(value);
		}
	}

	/**
	 * The Bernoulli number at the index provided.
	 *
	 * @param n the index, non-negative.
	 * @return the B_0=1 for n=0, B_1=-1/2 for n=1, B_2=1/6 for n=2 etc
	 * @author Richard J. Mathar
	 */
	public Rational at(int n) {
		if (n == 1) {
			return (new Rational(-1, 2));
		} else if (n % 2 != 0) {
			return Rational.ZERO;
		} else {
			final int nindx = n / 2;
			if (a.size() <= nindx) {
				for (int i = 2 * a.size(); i <= n; i += 2)
					set(i, doubleSum(i));
			}
			return a.elementAt(nindx);
		}
	}
	private Rational doubleSum(int n) {
		Rational resul = Rational.ZERO;
		for (int k = 0; k <= n; k++) {
			Rational jsum = Rational.ZERO;
			BigInteger bin = BigInteger.ONE;
			for (int j = 0; j <= k; j++) {
				BigInteger jpown = (new BigInteger(String.valueOf(j))).pow(n);
				if (j % 2 == 0) {
					jsum = jsum.add(bin.multiply(jpown));
				} else {
					jsum = jsum.subtract(bin.multiply(jpown));
				}
				bin = bin.multiply(new BigInteger(String.valueOf(k - j))).divide(new BigInteger(String.valueOf(j + 1)));
			}
			resul = resul.add(jsum.divide(new BigInteger(String.valueOf(k + 1))));
		}
		return resul;
	}


}
