package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.StringUtils;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Miscellaneous math functions taking in a single argument
 * 
 * @name Unary Math Functions
 * @pattern abs %number%
 * @pattern \\|%number%\\|
 * @pattern %number%!
 * @pattern factorial of %number%
 * @pattern (sqrt|square root of) %number%
 * @pattern floor[ed] %number%
 * @pattern ceil[ing|ed] %number%
 * @pattern round[ed] %number%
 * @pattern sin %number%
 * @pattern cos %number%
 * @pattern tan %number%
 * @pattern asin %number%
 * @pattern acos %number%
 * @pattern atan %number%
 * @pattern sinh %number%
 * @pattern cosh %number%
 * @pattern tanh %number%
 * @pattern ln %number%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprUnaryMathFunctions implements Expression<Number> {

	private static final PatternInfos<UnaryOperator<Number>> PATTERNS = new PatternInfos<>(
		new Object[][]{
			{"abs %number%|\\|%number%\\|", (UnaryOperator<Number>) NumberMath::abs},
			{"%number%!", (UnaryOperator<Number>) NumberMath::factorial},
			{"factorial of %number%", (UnaryOperator<Number>) NumberMath::factorial},
			{"(sqrt|square root of) %number%", (UnaryOperator<Number>) NumberMath::sqrt},
			{"floor[ed] %number%", (UnaryOperator<Number>) NumberMath::floor},
			{"ceil[ing|ed] %number%", (UnaryOperator<Number>) NumberMath::ceil},
			{"round[ed] %number%", (UnaryOperator<Number>) NumberMath::round},
			{"sin %number%", (UnaryOperator<Number>) NumberMath::sin},
			{"cos %number%", (UnaryOperator<Number>) NumberMath::cos},
			{"tan %number%", (UnaryOperator<Number>) NumberMath::tan},
			{"asin %number%", (UnaryOperator<Number>) NumberMath::asin},
			{"acos %number%", (UnaryOperator<Number>) NumberMath::acos},
			{"atan %number%", (UnaryOperator<Number>) NumberMath::atan},
			{"sinh %number%", (UnaryOperator<Number>) NumberMath::sinh},
			{"cosh %number%", (UnaryOperator<Number>) NumberMath::cosh},
			{"tanh %number%", (UnaryOperator<Number>) NumberMath::tanh},
			{"ln %number%", (UnaryOperator<Number>) NumberMath::ln}
		}
	);
	private int pattern;
	private Expression<Number> number;

	static {
		Main.getMainRegistration().addExpression(
			ExprUnaryMathFunctions.class,
			Number.class,
			true,
			PATTERNS.getPatterns()
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		number = (Expression<Number>) expressions[0];
		return true;
	}

	@Override
	public Number[] getValues(TriggerContext ctx) {
		return number.getSingle(ctx)
				.map(n -> new Number[]{ PATTERNS.getInfo(pattern).apply(n) })
				.orElse(new Number[0]);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		/*
		 * This is dirty, but at least it's better than switching over all cases
		 */
		String pat = PATTERNS.getPatterns()[pattern];
		String expr = number.toString(ctx, debug);
		if (StringUtils.count(pat, "(", "[") == 0) {
			return pat.replace("%number%", expr);
		} else {
			switch (pattern) {
				case 0:
					return "abs " + expr;
				case 1:
					return "factorial of " + expr;
				case 2:
					return "square root of " + expr;
				case 3:
					return "floored " + expr;
				case 4:
					return "ceiled " + expr;
				case 5:
					return "rounded " + expr;
				default:
					throw new IllegalStateException();
			}
		}
	}
}
