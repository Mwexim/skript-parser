package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.classes.DoubleOptional;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;
import io.github.syst3ms.skriptparser.util.math.NumberMath;

import java.math.BigDecimal;
import java.util.function.BinaryOperator;

/**
 * Miscellaneous math functions taking in two arguments.
 *
 * @name Binary Math Functions
 * @pattern log[arithm] [base] %number% of %number%
 * @pattern (root %number%|[the] %integer%(st|nd|rd|th) root) of %number%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprBinaryMathFunctions implements Expression<Number> {
	public static final PatternInfos<BinaryOperator<Number>> PATTERNS = new PatternInfos<>(
		new Object[][] {
				{"log[arithm] [base] %number% of %number%", (BinaryOperator<Number>) NumberMath::log},
				{"(root %number%|[the] %integer%(st|nd|rd|th) root) of %number%", (BinaryOperator<Number>) (r, n) -> {
					var root = r instanceof BigDecimal ? (BigDecimal) r : new BigDecimal(r.toString());
					if (root.equals(BigDecimal.ONE)) {
						return n;
					} else if (root.equals(BigDecimal.valueOf(2))) {
						return NumberMath.sqrt(n);
					}
					var numb = n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal(n.toString());
					BigDecimal res = BigDecimal.ONE.divide(root, BigDecimalMath.DEFAULT_CONTEXT);
					return BigDecimalMath.pow(numb, res, BigDecimalMath.DEFAULT_CONTEXT);
				}
				}
		}
	);

	static {
		Parser.getMainRegistration().addExpression(
			ExprBinaryMathFunctions.class,
			Number.class,
			true,
			PATTERNS.getPatterns()
		);
	}

	private Expression<Number> first, second;
	private int pattern;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		first = (Expression<Number>) expressions[0];
		second = (Expression<Number>) expressions[1];
		return true;
	}

	@Override
	public Number[] getValues(TriggerContext ctx) {
		DoubleOptional<? extends Number, ? extends Number> args = DoubleOptional.ofOptional(
				first.getSingle(ctx),
				second.getSingle(ctx)
		);
		BinaryOperator<Number> operator = PATTERNS.getInfo(pattern);
		return args.mapToOptional((f, s) -> new Number[] {operator.apply(f, s)}).orElse(new Number[0]);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		if (pattern == 0) {
			return "log " + first.toString(ctx, debug) + " of " + second.toString(ctx, debug);
		} else {
			return "root " + first.toString(ctx, debug) + " of " + second.toString(ctx, debug);
		}
	}
}
