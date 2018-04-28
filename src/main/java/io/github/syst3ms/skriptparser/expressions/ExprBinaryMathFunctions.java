package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.function.BinaryOperator;

public class ExprBinaryMathFunctions implements Expression<Number> {
	public static PatternInfos<BinaryOperator<Number>> PATTERNS = new PatternInfos<>(
		new Object[][] {
			{"log [base] %number% of %number%", (BinaryOperator<Number>) NumberMath::log},
			{"root %number% of %number%", (BinaryOperator<Number>) (n, r) -> {
					if (r.intValue() == 1) {
						return n;
					} else if (r.intValue() == 2) {
						return NumberMath.sqrt(n);
					} else {
						BigDecimal a = new BigDecimal(n.toString());
						BigDecimal b = BigDecimal.ONE.divide(new BigDecimal(r.toString()), BigDecimalMath.DEFAULT_CONTEXT);
						return BigDecimalMath.pow(a, b);
					}
				}
			}
		}
	);
	private int pattern;
	private Expression<Number> first, second;

	static {
		Main.getMainRegistration().addExpression(
			ExprBinaryMathFunctions.class,
			Number.class,
			true,
			PATTERNS.getPatterns()
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		pattern = matchedPattern;
		first = (Expression<Number>) expressions[0];
		second = (Expression<Number>) expressions[1];
		return true;
	}

	@Override
	public Number[] getValues(Event e) {
		Number f = first.getSingle(e);
		Number s = second.getSingle(e);
		if (f == null || s == null)
			return new Number[0];
		BinaryOperator<Number> operator = PATTERNS.getInfo(pattern);
		return new Number[]{operator.apply(f, s)};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (pattern == 0) {
			return "log " + first.toString(e, debug) + " of " + second.toString(e, debug);
		} else {
			return "root " + first.toString(e, debug) + " of " + second.toString(e, debug);
		}
	}
}
