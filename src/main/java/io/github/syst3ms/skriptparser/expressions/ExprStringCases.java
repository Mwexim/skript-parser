package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Converts a given string into a certain case type.
 * Click <a href="https://www.chaseadams.io/posts/most-common-programming-case-types/">here</a> to learn about all these cases.
 *
 * @name Cased String
 * @type EXPRESSION
 * @pattern %strings% in (upper|lower)[ ]case
 * @pattern %strings% in camel[ ]case
 * @pattern %strings% in pascal case
 * @pattern %strings% in snake case
 * @pattern %strings% in kebab case
 * @pattern %strings% in reverse[d] case
 * @pattern mirror[ed] %strings%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprStringCases implements Expression<String> {

	static {
		Main.getMainRegistration().addExpression(
				ExprStringCases.class,
				String.class,
				false,
				"%strings% in upper[ ]case",
				"%strings% in lower[ ]case",
				"%strings% in camel[ ]case",
				"%strings% in pascal case",
				"%strings% in snake case",
				"%strings% in kebab case",
				"(reverse[d] %strings%|%strings% in reverse[d] case)",
				"mirror[ed] %strings%");
	}

	private final static String[] CHOICES = {
			"upper", "lower", "camel", "pascal", "snake", "kebab", "reverse", "mirrored"
	};
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
		String[] strs = expr.getValues(ctx);
		if (strs.length == 0)
			return new String[0];

		for (int i = 0; i < strs.length; i++) {
			switch (pattern) {
				case 0:
					strs[i] = strs[i].toUpperCase();
					break;
				case 1:
					strs[i] = strs[i].toLowerCase();
					break;
				case 2:
					strs[i] = StringUtils.camelCase(strs[i], true);
					break;
				case 3:
					strs[i] = StringUtils.camelCase(strs[i], false);
					break;
				case 4:
					strs[i] = strs[i].toLowerCase().replaceAll(" ", "_");
					break;
				case 5:
					strs[i] = strs[i].toLowerCase().replaceAll(" ", "-");
					break;
				case 6:
					strs[i] = StringUtils.reverseCase(strs[i]);
					break;
				case 7:
					strs[i] = StringUtils.mirrored(strs[i]);
					break;
			}
		}
		return strs;
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return expr.toString(ctx, debug) + " in " + CHOICES[pattern] + " case";
	}

}
