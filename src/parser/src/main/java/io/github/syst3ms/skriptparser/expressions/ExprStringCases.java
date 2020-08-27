package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Converts a given string into a certain case type.
 * Click <a href="https://www.chaseadams.io/posts/most-common-programming-case-types/">here</a> to learn about all these cases.
 *
 * @name Cased String
 * @type EXPRESSION
 * @pattern %string% in (upper|lower)[ ]case
 * @pattern %string% (to|in) camel[ ]case
 * @pattern %string% (to|in) (pascal |capital[ized] camel[ ])case
 * @pattern %string% (to|in) snake case
 * @pattern %string% (to|in) snake case
 * @pattern (reverse[d] %string%|%string% (to|in) reverse[d] case)
 * @pattern mirror[ed] %string%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprStringCases implements Expression<String> {

	private final static PatternInfos<UnaryOperator<String>> PATTERNS = new PatternInfos<>(
			new Object[][]{
					{"%string% in upper[ ]case", (UnaryOperator<String>) String::toUpperCase},
					{"%string% in lower[ ]case", (UnaryOperator<String>) String::toLowerCase},
					{"%string% (to|in) camel[ ]case", (UnaryOperator<String>) str -> StringUtils.camelCase(str, true)},
					{"%string% (to|in) (pascal |capital[ized] camel[ ])case", (UnaryOperator<String>) str -> StringUtils.camelCase(str, false)},
					{"%string% (to|in) snake case", (UnaryOperator<String>) str -> str.toLowerCase().replaceAll("\\s+", "_")},
					{"%string% (to|in) kebab case", (UnaryOperator<String>) str -> str.toLowerCase().replaceAll("\\s+", "-")},
					{"(reverse[d] %string%|%string% (to|in) reverse[d] case)", (UnaryOperator<String>) StringUtils::reverseCase},
					{"mirror[ed] %string%", (UnaryOperator<String>) StringUtils::mirrored},
			}
	);

	static {
		Main.getMainRegistration().addExpression(
				ExprStringCases.class,
				String.class,
				true,
				PATTERNS.getPatterns()
		);
	}

	private Expression<String> expr;
	int pattern;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		expr = (Expression<String>) expressions[0];
		pattern = matchedPattern;
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		return expr.getSingle(ctx)
				.map(s -> new String[]{ PATTERNS.getInfo(pattern).apply(s) })
				.orElse(new String[0]);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		String caseType = "some";
		switch (pattern) {
			case 0:
				caseType = "upper";
				break;
			case 1:
				caseType = "lower";
				break;
			case 2:
				caseType = "camel";
				break;
			case 3:
				caseType = "pascal";
				break;
			case 4:
				caseType = "snake";
				break;
			case 5:
				caseType = "kebab";
				break;
			case 6:
				caseType = "reversed";
				break;
			case 7:
				caseType = "mirrored";
				break;
		}
		return expr.toString(ctx, debug) + " in " + caseType + " case";
	}

}
