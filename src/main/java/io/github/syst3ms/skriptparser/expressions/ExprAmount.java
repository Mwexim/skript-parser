package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

/**
 * The amount of elements in a given list.
 * Note that when getting the size of a list variable, it will only return the size of the first layer of elements.
 * If you want to get the whole list size, including nested layers in a variable, use the recursive size instead.
 *
 * @name Amount
 * @type EXPRESSION
 * @pattern [the] [recursive] (amount|number|size) of %~objects%
 * @pattern %~objects%'[s] [recursive] (amount|number|size)
 * @since ALPHA
 * @author Olyno, Mwexim
 */
public class ExprAmount extends PropertyExpression<Number, Object> {
	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprAmount.class,
				Number.class,
				true,
				"~objects",
				"[1:recursive] (amount|number|size)"
		);
	}

	private boolean recursive;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
		setOwner((Expression<Object>) expressions[0]);
		this.recursive = parseContext.getNumericMark() == 1;
		if (recursive && !(getOwner() instanceof Variable<?>)) {
			parseContext.getLogger().error("Getting the recursive size of an expression only applies to variables.", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Number[] getValues(TriggerContext ctx) {
		if (recursive) {
			Optional<Object> var = ((Variable<?>) getOwner()).getRaw(ctx);
			if (var.isPresent())
				return new Number[] {
						BigInteger.valueOf(getRecursiveSize((Map<String, ?>) var.get()))
			};
		}
		return new Number[] {BigInteger.valueOf(getOwner().getValues(ctx).length)};
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return (recursive ? "recursive " : "") + "size of " + getOwner().toString(ctx, debug);
	}

	@SuppressWarnings("unchecked")
	private static long getRecursiveSize(Map<String, ?> map) {
		long count = 0;
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map)
				count += getRecursiveSize((Map<String, ?>) value);
			else
				count++;
		}
		return count;
	}
}
