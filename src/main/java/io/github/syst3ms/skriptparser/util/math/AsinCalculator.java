package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Calculates arc sinus using the Maclaurin series.
 * 
 * <p>See <a href="https://de.wikipedia.org/wiki/Taylorreihe">Wikipedia: Taylorreihe</a></p>
 * 
 * <p>No argument checking or optimizations are done.
 * This implementation is <strong>not</strong> intended to be called directly.</p>
 *
 * I do not claim ownership of this code, it is the intellectual property of <a href="github.com/eobermuhlner">@obermuhlner</a>.
 * @author @obermuhlner
 */
public class AsinCalculator extends SeriesCalculator {

	public static final AsinCalculator INSTANCE = new AsinCalculator();
	
	private int n = 0;
	private BigRational factorial2n = BigRational.ONE;
	private BigRational factorialN = BigRational.ONE;
	private BigRational fourPowerN = BigRational.ONE;
	
	private AsinCalculator() {
	}
	
	@Override
	protected BigRational getCurrentFactor() {
		return factorial2n.divide(fourPowerN.multiply(factorialN).multiply(factorialN).multiply(2 * n + 1));
	}
	
	@Override
	protected void calculateNextFactor() {
		n++;
		factorial2n = factorial2n.multiply(2 * n - 1).multiply(2 * n);
		factorialN = factorialN.multiply(n);
		fourPowerN = fourPowerN.multiply(4);
	}
	
	@Override
	protected PowerIterator createPowerIterator(BigDecimal x, MathContext mathContext) {
		return new PowerTwoNPlusOneIterator(x, mathContext);
	}
}
