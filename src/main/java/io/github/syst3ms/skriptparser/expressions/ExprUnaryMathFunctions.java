package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.StringUtils;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class ExprUnaryMathFunctions implements Expression<Number> {
	public static final PatternInfos<UnaryOperator<Number>> PATTERNS = new PatternInfos<>(
		new Object[][]{
			{"abs %number%", (UnaryOperator<Number>) NumberMath::abs},
			{"\\|%number%\\|", (UnaryOperator<Number>) NumberMath::abs},
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
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		pattern = matchedPattern;
		number = (Expression<Number>) expressions[0];
		return true;
	}

	@Override
	public Number[] getValues(TriggerContext ctx) {
		Number num = number.getSingle(ctx);
		if (num == null)
			return new Number[0];
		return new Number[]{PATTERNS.getInfo(pattern).apply(num)};
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		/*
		 * I know this is dirty as hell, but at least it's better than switching
		 * over ALL of them
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
