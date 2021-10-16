package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;
import io.github.syst3ms.skriptparser.util.math.NumberMath;

/**
 * Check if a given number is a prime number.
 * This means that for a number {@code n},
 * there does not exist a number {@code a} such that
 * {@code a < n} and {@code n % a = 0}, except for {@code a = 1}.
 *
 * @name Is Prime
 * @type CONDITION
 * @pattern %numbers% (is|are)[ not|n't] [a] prime [number[s]]
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprIsPrime extends PropertyConditional<Number> {
	static {
		Parser.getMainRegistration().newPropertyConditional(CondExprIsPrime.class, "numbers", ConditionalType.BE, "[a] prime [number[s]]")
				.addData(PROPERTY_IDENTIFIER, "prime")
				.register();
	}

	@Override
	public boolean check(Number performer) {
		var bd = BigDecimalMath.getBigDecimal(performer);
		return bd.signum() != -1
				&& BigDecimalMath.isIntValue(bd)
				&& NumberMath.isPrime(BigDecimalMath.getBigInteger(bd));
	}
}
