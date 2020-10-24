package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Basic list operators with the following behavior:
 * <ul>
 *      <li>{@code pop} removes the last element and returns that removed element.</li>
 *      <li>{@code shift/poll} removes the first element and returns that removed element.</li>
 *      <li>{@code splice} removes elements in a certain bound and returns those removed elements.</li>
 * </ul>
 * These are all changing operators, which mean they apply the operator on the source list
 * and return other elements accordingly to the operator type. If the list is not changeable,
 * then these operators will not work.
 * Note that indices in Skript start at one and that the upper bound of the {@code splice} operator
 * is inclusive.
 * If the lower/upper bound is not a part of the list, an empty list will be returned, except for when the
 * upper bound is higher than the size of the list. In that case, the list size will be taken as upper bound.
 *
 * @name List Operators
 * @type EXPRESSION
 * @pattern pop %objects%
 * @pattern (shift|poll) %objects%
 * @pattern splice %objects% from %integer% [1:to %integer%]
 * @since ALPHA
 * @author Mwexim
 * @see ExprElement
 */
public class ExecExprListOperators extends ExecutableExpression<Object> {

	static {
		Parser.getMainRegistration().addSelfRegisteringElement(
				ExecExprListOperators.class,
				Object.class,
				false,
				"pop %objects%",
				"(shift|poll) %objects%",
				"splice %objects% from %integer% [1:to %integer%]"
		);
	}

	private int pattern;
	private Expression<Object> list;
	private Expression<BigInteger> lower;
	private Expression<BigInteger> upper;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		list = (Expression<Object>) expressions[0];
		if (pattern == 2) {
			lower = (Expression<BigInteger>) expressions[1];
			if (parseContext.getParseMark() == 1)
				upper = (Expression<BigInteger>) expressions[2];
		}

		var logger = parseContext.getLogger();
		if (list.acceptsChange(ChangeMode.SET).isEmpty()) {
			logger.error(
					list.toString(null, logger.isDebug()) + " cannot be changed accordingly",
					ErrorType.SEMANTIC_ERROR
			);
			return false;
		} else if (list.isSingle()) {
			logger.error(
					list.toString(null, logger.isDebug()) + " represents a single value",
					ErrorType.SEMANTIC_ERROR,
					"List operators only work on multiple objects at the same time"
			);
			return false;
		}
		return true;
	}

	@Override
	public Object[] getValues(TriggerContext ctx) {
		var values = list.getValues(ctx);
		if (values.length == 0)
			return new Object[0];

		switch (pattern) {
			case 0:
				list.change(ctx, Arrays.copyOfRange(values, 0, values.length - 1), ChangeMode.SET);
				return new Object[] {values[values.length - 1]};
			case 1:
				list.change(ctx, Arrays.copyOfRange(values, 1, values.length), ChangeMode.SET);
				return new Object[] {values[0]};
			case 2:
				var low = lower.getSingle(ctx);
				if (low.isEmpty() || low.get().compareTo(BigInteger.ZERO) <= 0) {
					list.change(ctx, new Object[0], ChangeMode.SET);
					return new Object[0];
				}
				int b1 = low.get().intValue() - 1;
				int b2 = upper != null
						? upper.getSingle(ctx)
								.filter(u -> u.compareTo(BigInteger.valueOf(values.length)) <= 0)
								.map(u -> (BigInteger) u)
								.orElse(BigInteger.valueOf(values.length)).intValue()
						: values.length;
				if (b1 > b2) {
					list.change(ctx, new Object[0], ChangeMode.SET);
					return new Object[0];
				} else if (b1 == b2) {
					return new Object[0]; // There's nothing to change here.
				}
				list.change(
						ctx,
						Stream.of(Arrays.copyOfRange(values, 0, b1), Arrays.copyOfRange(values, b2, values.length))
								.flatMap(Stream::of)
								.toArray(),
						ChangeMode.SET
				);
				return Arrays.copyOfRange(values, b1, b2);
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public boolean isSingle() {
		return pattern == 0 || pattern == 1;
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		switch (pattern) {
			case 0:
				return "pop " + list.toString(ctx, debug);
			case 1:
				return "poll " + list.toString(ctx, debug);
			case 2:
				return "splice " + list.toString(ctx, debug) + " from " + lower.toString(ctx, debug)
						+ (upper != null ? upper.toString(ctx, debug) : "");
			default:
				throw new IllegalStateException();
		}
	}
}
