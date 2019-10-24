package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * {@link PowerIterator} to calculate x<sup>2*n+1</sup>.
 *
 * I do not claim ownership of this code, it is the intellectual property of <a href="github.com/eobermuhlner">@obermuhlner</a>.
 * @author @obermuhlner
 */
public class PowerTwoNPlusOneIterator implements PowerIterator {

	private final MathContext mathContext;

	private final BigDecimal xPowerTwo;

	private BigDecimal powerOfX;

	public PowerTwoNPlusOneIterator(BigDecimal x, MathContext mathContext) {
		this.mathContext = mathContext;
		
		xPowerTwo = x.multiply(x, mathContext);
		powerOfX = x;
	}
	
	@Override
	public BigDecimal getCurrentPower() {
		return powerOfX;
	}

	@Override
	public void calculateNextPower() {
		powerOfX = powerOfX.multiply(xPowerTwo, mathContext);
	}
}