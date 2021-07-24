package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Basic list operators that return the following elements:
 * <ul>
 *      <li>{@code pop}: the last element</li>
 *      <li>{@code shift/poll}: the first element</li>
 *      <li>{@code extract}: a specific (or just the first/last) element</li>
 *      <li>{@code splice}: elements in a certain bound</li>
 * </ul>
 * However, this syntax can also be used as an effect. If this is the case, instead of returning
 * the elements, specified above, it will <b>remove</b> them from the list, similar to their
 * JavaScript counter-parts.
 * <br>
 * Note that indices in Skript start at one and that both the lower and upper bounds are inclusive.
 * The step function can be used in the {@code splice} pattern to skip over certain values. Note that
 * when a negative step function is used, the list is reversed as well as the lower and upper bounds,
 * which means the lower bound must be higher than the upper bound.
 *
 * @name List Operators
 * @type EFFECT/EXPRESSION
 * @pattern extract [the] (last|first|%integer%(st|nd|rd|th)) element out [of] %objects%
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

	// 0 = last, 1 = first, 2 = indexed, 3 = spliced
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
				type = parseContext.getNumericMark();
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
				switch (parseContext.getNumericMark()) {
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
					"The expression '"
							+ list.toString(TriggerContext.DUMMY, logger.isDebug())
							+ "' cannot be changed",
					ErrorType.SEMANTIC_ERROR
			);
			return false;
		} else if (list.isSingle()) {
			logger.error(
					list.toString(TriggerContext.DUMMY, logger.isDebug()) + " represents a single value",
					ErrorType.SEMANTIC_ERROR,
					"List operators only work on multiple objects at the same time"
			);
			return false;
		}
		return true;
	}

	@Override
	public Object[] getValues(TriggerContext ctx, boolean isEffect) {
		var values = list.getValues(ctx);
		if (values.length == 0)
			return new Object[0];

		switch (type) {
			case 0:
				if (isEffect)
					list.change(ctx, Arrays.copyOfRange(values, 0, values.length - 1), ChangeMode.SET);
				return new Object[] {values[values.length - 1]};
			case 1:
				if (isEffect)
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

				// When used as an effect
				if (isEffect) {
					var skipped = new Object[values.length - 1];
					for (int i = 0, k = 0; i < values.length; i++) {
						if (i == ind) {
							continue;
						}
						skipped[k++] = values[i];
					}
					list.change(ctx, skipped, ChangeMode.SET);
				}
				return new Object[] {values[ind]};
			case 3:
				var low = lower != null
						? lower.getSingle(ctx).filter(n -> n.signum() > 0).map(n -> n.intValue() - 1).orElse(-1)
						: 0;
				var up = upper != null
						? upper.getSingle(ctx).filter(n -> n.compareTo(BigInteger.valueOf(values.length)) <= 0).map(BigInteger::intValue).orElse(values.length)
						: values.length;
				var st = step != null
						? step.getSingle(ctx).filter(n -> n.signum() != 0 && n.compareTo(BigInteger.valueOf(-values.length)) >= 0 && n.compareTo(BigInteger.valueOf(values.length)) <= 0).map(BigInteger::intValue).orElse(0)
						: 1;

				if (st < 0) {
					CollectionUtils.reverseArray(values);
					st = -st;
					int temp = low;
					low = up - 1;
					up = temp + 1;
				}
				if (low == -1 || up == -1 || up == 0 || st == 0 || low > up) {
					if (isEffect)
						list.change(ctx, new Object[0], ChangeMode.SET);
					return new Object[0];
				} else if (low == up) {
					// Nothing to change
					return new Object[0];
				}

				var spliced = new ArrayList<>();
				var changed = new ArrayList<>();
				for (int i = 0; i < values.length; i++) {
					if (i >= low && i < up && (i - low) % st == 0) {
						spliced.add(values[i]);
					} else {
						changed.add(values[i]);
					}
				}

				if (isEffect)
					list.change(ctx, changed.toArray(), ChangeMode.SET);
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
	public String toString(TriggerContext ctx, boolean debug) {
		switch (type) {
			case 0:
				return "pop " + list.toString(ctx, debug);
			case 1:
				return "poll " + list.toString(ctx, debug);
			case 2:
				return "extract element number " + index.toString(ctx, debug) + " from " + list.toString(ctx, debug);
			case 3:
				return "splice " + list.toString(ctx, debug) + " from " + lower.toString(ctx, debug)
						+ (upper != null ? upper.toString(ctx, debug) : "");
			default:
				throw new IllegalStateException();
		}
	}
}
