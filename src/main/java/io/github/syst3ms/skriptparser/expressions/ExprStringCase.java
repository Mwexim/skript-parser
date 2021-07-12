package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.Arrays;

/**
 * Converts a given string into a certain case type.
 * Click <a href="https://www.chaseadams.io/posts/most-common-programming-case-types/">here</a> to learn about all these cases.
 *
 * @name Cased String
 * @type EXPRESSION
 * @pattern %strings% in (upper|lower)[ ]case
 * @pattern "(upper|lower)[ ]case %strings%",
 * @pattern "capitali(s|z)e[d] %strings%",
 * @pattern "%strings% in [(0:lenient|strict) ](proper|title)[ ]case",
 * @pattern "[(0:lenient|strict) ](proper|title)[ ]case %strings%",
 * @pattern "%strings% in [(0:lenient|strict) ]camel[ ]case",
 * @pattern "[(0:lenient|strict) ]camel[ ]case %strings%",
 * @pattern "%strings% in [(0:lenient|strict) ]pascal[ ]case",
 * @pattern "[(0:lenient|strict) ]pascal[ ]case %strings%",
 * @pattern "%strings% in [(upper|lower|capital|screaming)[ ]]snake[ ]case",
 * @pattern "[(upper|lower|capital|screaming)[ ]]snake[ ]case %strings%",
 * @pattern "%strings% in [(upper|lower|capital)[ ]]kebab[ ]case",
 * @pattern "[(upper|lower|capital)[ ]]kebab[ ]case %strings%",
 * @pattern "(reverse[d]|mirror[ed]) %strings%"
 * @since ALPHA
 * @author Mwexim, WealthyTurtle
 */
public class ExprStringCase implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprStringCase.class,
				String.class,
				true,
				"%strings% in (1:upper|2:lower)[ ]case",
				"(1:upper|2:lower)[ ]case %strings%",
				"capitali(s|z)e[d] %strings%",
				"%strings% in [(0:lenient|3:strict) ](proper|title)[ ]case",
				"[(0:lenient|3:strict) ](proper|title)[ ]case %strings%",
				"%strings% in [(0:lenient|3:strict) ]camel[ ]case",
				"[(0:lenient|3:strict) ]camel[ ]case %strings%",
				"%strings% in [(0:lenient|3:strict) ]pascal[ ]case",
				"[(0:lenient|3:strict) ]pascal[ ]case %strings%",
				"%strings% in [(1:upper|2:lower|1:capital|1:screaming)[ ]]snake[ ]case",
				"[(1:upper|2:lower|1:capital|1:screaming)[ ]]snake[ ]case %strings%",
				"%strings% in [(1:upper|2:lower|1:capital)[ ]]kebab[ ]case",
				"[(1:upper|2:lower|1:capital)[ ]]kebab[ ]case %strings%",
				"(reverse[d]|mirror[ed]) %strings%"
		);
	}

	private Expression<String> expr;
	// 0: no change, 1: upper case, 2: lower case, 3: strict
	private int mode;
	// 0: basic case change, 1: proper/capitalized, 2: camel, 3: pascal,
	// 4: snake, 5: kebab, 6: reversed
	private int type;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		expr = (Expression<String>) expressions[0];
		mode = parseContext.getNumericMark();
		switch (matchedPattern) {
			case 0:
			case 1:
				type = 0;
				break;
			case 2:
				type = 0;
				mode = 1;
				break;
			case 3:
			case 4:
				type = 1;
				break;
			case 5:
			case 6:
				type = 2;
				break;
			case 7:
			case 8:
				type = 3;
				break;
			case 9:
			case 10:
				type = 4;
				break;
			case 11:
			case 12:
				type = 5;
				break;
			case 13:
				type = 6;
				break;
			default:
				throw new IllegalStateException();
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		return Arrays.stream(expr.getValues(ctx))
				.map(val -> {
					switch (type) {
						case 0: // Basic case change
							return mode == 1 ? val.toUpperCase() : val.toLowerCase();
						case 1: // Title case
							return StringUtils.toTitleCase(
									mode == 3 ? val.toLowerCase() : val,
									true
							);
						case 2: // Camel case
							return StringUtils.toCamelCase(
									mode == 3 ? val.toLowerCase() : val,
									true
							);
						case 3: // Pascal case
							return StringUtils.toCamelCase(
									mode == 3 ? val.toLowerCase() : val,
									false
							);
						case 4: // Snake case
							return StringUtils.toSnakeCase(val, mode);
						case 5: // Kebab case
							return StringUtils.toKebabCase(val, mode);
						case 6: // Reversed
							return StringUtils.mirrored(val);
						default:
							throw new IllegalStateException();
					}
				})
				.toArray(String[]::new);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		switch (type) {
			case 0: // Basic Case Change
				return expr.toString(ctx, debug) + " in " + (mode == 1 ? "uppercase" : "lowercase");
			case 1: // Proper Case
				return expr.toString(ctx, debug) + " in " + (mode == 3 ? "strict" : "lenient") + " proper case";
			case 2: // Camel Case
				return expr.toString(ctx, debug) + " in " + (mode == 3 ? "strict" : "lenient") + " camel case";
			case 3: // Pascal Case
				return expr.toString(ctx, debug) + " in " + (mode == 3 ? "strict" : "lenient") + " pascal case";
			case 4: // Snake Case
				return expr.toString(ctx, debug) + " in " + (mode == 0 ? "" : (mode == 1 ? "upper " : "lower ")) + "snake case";
			case 5: // Kebab Case
				return expr.toString(ctx, debug) + " in " + (mode == 0 ? "" : (mode == 1 ? "upper " : "lower ")) + "kebab case";
			case 6:
				return "reversed " + expr.toString(ctx, debug);
			default:
				throw new IllegalStateException();
		}
	}
}
