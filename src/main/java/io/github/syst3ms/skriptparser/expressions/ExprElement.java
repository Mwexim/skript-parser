package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A certain element or multiple elements out of a list of objects.
 * Remember that in Skript, indices start from 1.
 *
 * @name Element
 * @type EXPRESSION
 * @pattern ([the] first|[the] last|[a] random|[the] %integer%(st|nd|rd|th)) element out [of] %objects%
 * @pattern [the] (first|last) %integer% elements out [of] %objects%
 * @pattern %objects%\[%integer%\]
 * @since ALPHA
 * @author Mwexim
 */
public class ExprElement implements Expression<Object> {

	static {
		Parser.getMainRegistration().addExpression(
				ExprElement.class,
				Object.class,
				true,
				"(0:[the] first|1:[the] last|2:[a] random|3:[the] %integer%(st|nd|rd|th)) element out [of] %objects%",
				"[the] (0:first|1:last) %integer% elements out [of] %objects%",
				"%objects%\\[%integer%\\]");
	}

	private static final ThreadLocalRandom random = ThreadLocalRandom.current();

	private Expression<Object> expr;
	private Expression<Long> range;
	private int pattern;
	private int parseMark;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		parseMark = parseContext.getParseMark();

		switch (pattern) {
			case 0:
				if (parseMark == 3) {
					range = (Expression<Long>) expressions[0];
					expr = (Expression<Object>) expressions[1];
				} else {
					expr = (Expression<Object>) expressions[0];
				}
				break;
			case 1:
				range = (Expression<Long>) expressions[0];
				expr = (Expression<Object>) expressions[1];
				break;
			default:
				expr = (Expression<Object>) expressions[0];
				range = (Expression<Long>) expressions[1];
				break;
		}
		return true;
	}

	@Override
	public boolean isSingle() {
		return pattern != 0;
	}

	@Override
	public Object[] getValues(TriggerContext ctx) {
		Object[] values = expr.getValues(ctx);
		if (values.length == 0)
			return new Object[0];
		int index = 0;

		if (range != null) {
			if (range.getSingle(ctx).isEmpty())
				return new Object[0];
			Long r = range.getSingle(ctx).get();
			index = r.intValue();
			if (index > values.length && pattern == 1) {
				return values;
			} else if (index > values.length || index <= 0) {
				return new Object[0];
			}
		}

		switch (pattern) {
			case 0:
				switch (parseMark) {
					case 0:
						return new Object[] {values[0]};
					case 1:
						return new Object[] {values[values.length - 1]};
					case 2:
						return new Object[] {values[NumberMath.random(0, values.length - 1, true, random).intValue()]};
					case 3:
						return new Object[] {values[index - 1]};
					default:
						return new Object[0];
				}
			case 1:
				if (parseMark == 0) {
					return Arrays.copyOfRange(values, 0, index);
				} else {
					return Arrays.copyOfRange(values, values.length - index, values.length);
				}
			case 2:
				return new Object[] {values[index - 1]};
			default:
				return new Object[0];
		}
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		switch (pattern) {
			case 0:
			case 2:
				return "element out of " + expr.toString(ctx, debug);
			case 1:
				return "elements out of " + expr.toString(ctx, debug);
			default:
				return "";
		}
	}
}
