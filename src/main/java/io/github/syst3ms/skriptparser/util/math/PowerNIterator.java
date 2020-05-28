package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * {@link PowerIterator} to calculate x<sup>n</sup>.
 *
 * I do not claim ownership of this code, it is the intellectual property of <a href="github.com/eobermuhlner">@obermuhlner</a>.
 * @author @obermuhlner
 */
public class PowerNIterator implements PowerIterator {

	private final BigDecimal x;

	private final MathContext mathContext;

	private BigDecimal powerOfX;

	public PowerNIterator(BigDecimal x, MathContext mathContext) {
		this.x = x;
		this.mathContext = mathContext;
		
		powerOfX = BigDecimal.ONE;
	}
	
	@Override
	public BigDecimal getCurrentPower() {
		return powerOfX;
	}

	@Override
	public void calculateNextPower() {
		powerOfX = powerOfX.multiply(x, mathContext);
	}
}