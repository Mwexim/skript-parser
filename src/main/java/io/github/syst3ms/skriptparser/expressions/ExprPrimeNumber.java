package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.math.NumberMath;

import java.math.BigInteger;

/**
 * Returns the nth prime, where n is the given number.
 * Note that this expression may have slow results when used with a high number.
 *
 * @name Prime
 * @type EXPRESSION
 * @pattern [the] %integer%(st|nd|rd|th) prime [number]
 * @since ALPHA
 * @author Mwexim
 */
public class ExprPrimeNumber implements Expression<Number> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprPrimeNumber.class,
				Number.class,
				true,
				"[the] %integer%(st|nd|rd|th) prime [number]"
		);
	}

	private Expression<BigInteger> ordinal;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		ordinal = (Expression<BigInteger>) expressions[0];
		return true;
	}

	@Override
	public Number[] getValues(TriggerContext ctx) {
		return ordinal.getSingle(ctx)
				.filter(n -> n.compareTo(BigInteger.ZERO) > 0)
				.map(n -> {
					if (NumberMath.getCachedPrimes().size() >= n.intValue()) {
						return new Number[] {
								BigInteger.valueOf(NumberMath.getCachedPrimes().get(n.intValue() - 1))
						};
					}
					int candidate, count;
					for (candidate = 2, count = 0; count < n.intValue(); candidate++) {
						if (NumberMath.isPrime(BigInteger.valueOf(candidate)))
							count++;
					}
					return new Number[] {BigInteger.valueOf(candidate - 1)};
				})
				.orElse(new Number[0]);

	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "prime number " + ordinal.toString(ctx, debug);
	}
}
