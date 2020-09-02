package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Calculates exp using the Maclaurin series.
 * 
 * <p>See <a href="https://de.wikipedia.org/wiki/Taylorreihe">Wikipedia: Taylorreihe</a></p>
 * 
 * <p>No argument checking or optimizations are done.
 * This implementation is <strong>not</strong> intended to be called directly.</p>
 *
 * I do not claim ownership of this code, it is the intellectual property of <a href="github.com/eobermuhlner">@obermuhlner</a>.
 * @author @obermuhlner
 */
public class ExpCalculator extends SeriesCalculator {

	public static final ExpCalculator INSTANCE = new ExpCalculator();
	
	private int n = 0;
	private BigRational oneOverFactorialOfN = BigRational.ONE;
	
	private ExpCalculator() {
		// prevent instances
	}

	@Override
	protected BigRational getCurrentFactor() {
		return oneOverFactorialOfN;
	}
	
	@Override
	protected void calculateNextFactor() {
		n++;
		oneOverFactorialOfN = oneOverFactorialOfN.divide(n);
	}

	@Override
	protected PowerIterator createPowerIterator(BigDecimal x, MathContext mathContext) {
		return new PowerNIterator(x, mathContext);
	}
}
