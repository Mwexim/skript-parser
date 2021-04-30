package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.classes.DoubleOptional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Joins multiple strings together using a delimiter,
 * splits a given string using a delimiter,
 * or splits that string in multiple string of a certain length.
 * Note that for the latter, the last string may contain less characters that specified.
 *
 * @name Join/Split
 * @type EXPRESSION
 * @pattern (concat[enate]|join) %strings% [(with|using|by) [[the] delimiter] %string%]
 * @pattern (split %string%|%string% split) (at|using|by) [[the] delimiter] %string%
 * @pattern (split %string%|%string% split) (with|using|by|every) %integer% [char[acter][s]]
 * @since ALPHA
 * @author Mwexim
 */
public class ExprStringSplitJoin implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprStringSplitJoin.class,
				String.class,
				false,
				"(concat[enate]|join) %strings% [1:(with|using|by) [[the] delimiter] %string%]",
				"(split %string%|%string% split) (at|using|by) [[the] delimiter] %string%",
				"(split %string%|%string% split) (with|using|by|every) %integer% [char[acter]][s]"
		);
	}

	private Expression<String> expr;
	private Expression<String> delimiter;
	private Expression<BigInteger> chars;

	private int pattern;
	private boolean delimiterPresent;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		delimiterPresent = parseContext.getParseMark() == 1;
		expr = (Expression<String>) expressions[0];
		if (pattern == 0) {
			if (delimiterPresent) {
				delimiter = (Expression<String>) expressions[1];
			}
		} else if (pattern == 1) {
			delimiter = (Expression<String>) expressions[1];
		} else {
			chars = (Expression<BigInteger>) expressions[1];
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		if (pattern == 0) {
			String[] strs = expr.getValues(ctx);
			String del = delimiterPresent ? delimiter.getSingle(ctx).orElse(null) : "";
			if (strs.length == 0 || del == null) {
				return new String[0];
			}
			return new String[]{String.join(del, strs)};
		} else if (pattern == 1) {
			return DoubleOptional.ofOptional(expr.getSingle(ctx), delimiter.getSingle(ctx))
					.mapToOptional((str, del) -> str.split(Pattern.quote(del)))
					.orElse(new String[0]);
		} else {
			return DoubleOptional.ofOptional(expr.getSingle(ctx), chars.getSingle(ctx))
					.mapToOptional((str, c) -> {
						List<String> ret = new ArrayList<>();
						int i = 0;
						while (i < str.length()) {
							ret.add(str.substring(i, Math.min(i + c.intValue(), str.length())));
							i += c.intValue();
						}
						return ret.toArray(new String[0]);
					})
					.orElse(new String[0]);
		}
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		if (pattern == 0) {
			return "join " + expr.toString(ctx, debug) + (delimiterPresent ? " using " + delimiter.toString(ctx, debug) : "");
		} else if (pattern == 1) {
			return "split " + expr.toString(ctx, debug) + " using " + delimiter.toString(ctx, debug);
		}
		return "split " + expr.toString(ctx, debug) + " using " + chars.toString(ctx, debug) + " characters";
	}
}
