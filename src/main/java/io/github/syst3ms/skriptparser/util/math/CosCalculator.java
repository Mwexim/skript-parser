package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Calculates cosinus using the Maclaurin series.
 * 
 * <p>See <a href="https://de.wikipedia.org/wiki/Taylorreihe">Wikipedia: Taylorreihe</a></p>
 * 
 * <p>No argument checking or optimizations are done.
 * This implementation is <strong>not</strong> intended to be called directly.</p>
 *
 * I do not claim ownership of this code, it is the intellectual property of <a href="github.com/eobermuhlner">@obermuhlner</a>.
 * @author @obermuhlner
 */
public class CosCalculator extends SeriesCalculator {

	public static final CosCalculator INSTANCE = new CosCalculator();
	
	private int n = 0;
	private boolean negative = false;
	private BigRational factorial2n = BigRational.ONE;
	
	private CosCalculator() {
		super(true);
	}
	
	@Override
	protected BigRational getCurrentFactor() {
		var factor = factorial2n.reciprocal();
		if (negative) {
			factor = factor.negate();
		}
		return factor;
	}
	
	@Override
	protected void calculateNextFactor() {
		n++;
		factorial2n = factorial2n.multiply(2 * n - 1).multiply(2 * n);
		negative = !negative;
	}
	
	@Override
	protected PowerIterator createPowerIterator(BigDecimal x, MathContext mathContext) {
		return new PowerTwoNIterator(x, mathContext);
	}
}
