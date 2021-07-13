package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
 * @pattern (split %string%|%string% split) (at|using|by) [[the] (delimiter|regex [pattern]] %string%
 * @pattern (split %string%|%string% split) (using|every) %integer% [char[acter][s]]
 * @since ALPHA
 * @author Mwexim
 */
public class ExprStringSplitJoin implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprStringSplitJoin.class,
				String.class,
				false,
				"(concat[enate]|join) %strings% [(with|using|by) [[the] delimiter] %string%]",
				"(split %string%|%string% split) (at|using|by) [[the] (delimiter|1:regex [pattern])] %string%",
				"(split %string%|%string% split) (using|every) %integer% [char[acter][s]]"
		);
	}

	private Expression<String> expression;
	@Nullable
	private Expression<String> delimiter;
	private Expression<BigInteger> step;

	private int pattern;
	private boolean regex;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		expression = (Expression<String>) expressions[0];
		pattern = matchedPattern;
		regex = parseContext.getNumericMark() == 1;
		switch (pattern) {
			case 0:
				if (expressions.length == 2) {
					delimiter = (Expression<String>) expressions[1];
				}
				break;
			case 1:
				delimiter = (Expression<String>) expressions[1];
				break;
			case 2:
				step = (Expression<BigInteger>) expressions[1];
				break;
			default:
				throw new IllegalStateException();
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		switch (pattern) {
			case 0:
				return new String[] {String.join(
						delimiter != null ? delimiter.getSingle(ctx).map(val -> (String) val).orElse("") : "",
						expression.getValues(ctx)
				)};
			case 1:
				assert delimiter != null;
				return DoubleOptional.ofOptional(expression.getSingle(ctx), delimiter.getSingle(ctx))
						.mapToOptional((val, separator) -> val.split(regex ? separator : Pattern.quote(separator)))
						.orElse(new String[0]);
			case 2:
				return DoubleOptional.ofOptional(expression.getSingle(ctx), step.getSingle(ctx))
						.map(Function.identity(), BigInteger::intValue)
						.mapToOptional((val, step) -> {
							List<String> ret = new ArrayList<>();
							for (int i = 0; i < val.length(); i += step) {
								ret.add(val.substring(i, Math.min(i + step, val.length())));
							}
							return ret.toArray(new String[0]);
						})
						.orElse(new String[0]);
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		switch (pattern) {
			case 0:
				return "join " + expression.toString(ctx, debug) + (delimiter != null ? " using " + delimiter.toString(ctx, debug) : "");
			case 1:
				assert delimiter != null;
				return "split " + expression.toString(ctx, debug) + " using " + delimiter.toString(ctx, debug);
			case 2:
				return "split " + expression.toString(ctx, debug) + " every " + step.toString(ctx, debug) + " characters";
			default:
				throw new IllegalStateException();
		}
	}
}
