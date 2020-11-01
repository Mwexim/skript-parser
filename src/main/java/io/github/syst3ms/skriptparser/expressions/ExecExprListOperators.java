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
import java.util.ArrayList;
import java.util.Arrays;

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
 * @pattern splice %objects% (from %integer% to %integer%|starting (at|from) %integer%|up to %integer%) [[with] step %integer%]
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
				"extract [the] (0:last|1:first|2:%integer%(st|nd|rd|th)) element out [of] %objects%",
				"pop %objects%",
				"(shift|poll) %objects%",
				"splice %objects% (0:from %integer% to %integer%|1:starting (at|from) %integer%|2:up to %integer%) [[with] step %integer%]"
		);
	}

	private int type;
	private Expression<Object> list;
	private Expression<BigInteger> index;
	private Expression<BigInteger> lower;
	private Expression<BigInteger> upper;
	private Expression<BigInteger> step;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		switch (matchedPattern) {
			case 0:
				type = parseContext.getParseMark();
				list = (Expression<Object>) (type == 2 ? expressions[1] : expressions[0]);
				if (type == 2)
					index = (Expression<BigInteger>) expressions[0];
				break;
			case 1:
			case 2:
				type = matchedPattern - 1;
				list = (Expression<Object>) expressions[0];
				break;
			case 3:
				type = 3;
				list = (Expression<Object>) expressions[0];
				switch (parseContext.getParseMark()) {
					case 0:
						lower = (Expression<BigInteger>) expressions[1];
						upper = (Expression<BigInteger>) expressions[2];
						if (expressions.length == 4)
							step = (Expression<BigInteger>) expressions[3];
						break;
					case 1:
						lower = (Expression<BigInteger>) expressions[1];
						if (expressions.length == 3)
							step = (Expression<BigInteger>) expressions[2];
						break;
					case 2:
						upper = (Expression<BigInteger>) expressions[1];
						if (expressions.length == 3)
							step = (Expression<BigInteger>) expressions[2];
						break;
					default:
						throw new IllegalStateException();
				}
		}
		var logger = parseContext.getLogger();
		if (list.acceptsChange(ChangeMode.SET).isEmpty()) {
			logger.error(
					list.toString(null, logger.isDebug()) + " is static and cannot be changed",
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

		switch (type) {
			case 0:
				list.change(ctx, Arrays.copyOfRange(values, 0, values.length - 1), ChangeMode.SET);
				return new Object[] {values[values.length - 1]};
			case 1:
				list.change(ctx, Arrays.copyOfRange(values, 1, values.length), ChangeMode.SET);
				return new Object[] {values[0]};
			case 2:
				int ind = index.getSingle(ctx)
						.filter(n -> n.signum() > 0 && n.compareTo(BigInteger.valueOf(values.length)) <= 0)
						.map(n -> n.intValue() - 1)
						.orElse(-1);
				if (ind == -1) {
					return new Object[0];
				}
				var skipped = new Object[values.length - 1];
				for (int i = 0, k = 0; i < values.length; i++) {
					if (i == ind) {
						continue;
					}
					skipped[k++] = values[i];
				}
				list.change(ctx, skipped, ChangeMode.SET);
				return new Object[] {values[ind]};
			case 3:
				var low = lower != null
						? lower.getSingle(ctx).filter(n -> n.signum() > 0).map(n -> n.intValue() - 1).orElse(-1)
						: 0;
				var up = upper != null
						? upper.getSingle(ctx).filter(n -> n.compareTo(BigInteger.valueOf(values.length)) <= 0).map(BigInteger::intValue).orElse(values.length)
						: values.length;
				var st = step != null
						? step.getSingle(ctx).filter(n -> n.signum() > 0 && n.compareTo(BigInteger.valueOf(values.length)) <= 0).map(BigInteger::intValue).orElse(-1)
						: 1;

				if (low == -1 || up == -1 || up == 0 || st == -1 || low > up) {
					list.change(ctx, new Object[0], ChangeMode.SET);
					return new Object[0];
				} else if (low == up) {
					// Nothing to change
					return new Object[0];
				}
				var spliced = new ArrayList<>();
				var changed = new Object[values.length - up + low];
				for (int i = 0, k = 0; i < values.length; i++) {
					if (i >= low && i < up && (i - low) % st == 0) {
						spliced.add(values[i]);
					} else {
						changed[k++] = values[i];
					}
				}

				list.change(ctx, changed, ChangeMode.SET);
				return spliced.toArray(new Object[0]);
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public boolean isSingle() {
		return type == 0 || type == 1;
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		switch (type) {
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
