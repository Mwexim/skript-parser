package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.lang.lambda.ArgumentSection;
import io.github.syst3ms.skriptparser.lang.lambda.SkriptConsumer;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.ranges.Ranges;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * A section that iterates over a collection of elements
 */
public class SecLoop extends ArgumentSection {
	static {
		Parser.getMainRegistration().addSection(
				SecLoop.class,
				"loop %integer% times",
				"loop %objects%"
		);
	}

	private Expression<?> expr;
	private Expression<BigInteger> times;
	private SkriptConsumer<SecLoop> lambda;
	private boolean isNumericLoop;
	private final transient Map<TriggerContext, Object> current = new WeakHashMap<>();
	private final transient Map<TriggerContext, Iterator<?>> currentIter = new WeakHashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		isNumericLoop = matchedPattern == 0;
		if (isNumericLoop) {
			times = (Expression<BigInteger>) expressions[0];
			// We can do some certainty checks with Literals.
			if (times instanceof Literal<?>) {
				var t = ((Optional<BigInteger>) ((Literal<BigInteger>) times).getSingle()).orElse(BigInteger.ONE);
				if (t.intValue() <= 0) {
					parseContext.getLogger().error("Cannot loop a negative or zero amount of times", ErrorType.SEMANTIC_ERROR);
					return false;
				} else if (t.intValue() == 1) {
					parseContext.getLogger().error(
							"Cannot loop a single time",
							ErrorType.SEMANTIC_ERROR,
							"Remove this loop, because looping something once can be achieved without a loop-statement"
					);
					return false;
				}
			}
		} else {
			expr = expressions[0];
			if (expr.isSingle()) {
				parseContext.getLogger().error(
						"Cannot loop a single value",
						ErrorType.SEMANTIC_ERROR,
						"Remove this loop, because you clearly don't need to loop a single value"
				);
				return false;
			}
		}
		lambda = SkriptConsumer.create(this);
		return true;
	}

	@Override
	public Optional<? extends Statement> walk(TriggerContext ctx) {
		if (isNumericLoop) {
			var range = (BigInteger[]) times.getSingle(ctx)
					.filter(t -> t.compareTo(BigInteger.ZERO) > 0)
					.map(t -> Ranges.getRange(BigInteger.class).orElseThrow()
							.getFunction()
							.apply(BigInteger.ONE, t)) // Upper bound is inclusive
					.orElse(new BigInteger[0]);
			// We just set the looped expression to a range from 1 to the amount of times.
			// This allows the usage of 'loop-number' to get the current iteration
			expr = new SimpleLiteral<>(BigInteger.class, range);
		}

		Iterator<?> iter = currentIter.get(ctx);
		if (iter == null) {
			iter = expr instanceof Variable ? ((Variable<?>) expr).variablesIterator(ctx) : expr.iterator(ctx);
			if (iter != null) {
				if (iter.hasNext())
					currentIter.put(ctx, iter);
				else
					iter = null;
			}
		}
		Iterator<?> finalIter = iter;
		return Optional.ofNullable(iter)
				.filter(Iterator::hasNext)
				.map(it -> {
					lambda.accept(ctx, it.next());
					return (Statement) this;
				})
				.or(() -> {
					if (finalIter != null)
						currentIter.remove(ctx);
					return getNext();
				});
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "loop " + (isNumericLoop ? times.toString(ctx, debug) + " times" : expr.toString(ctx, debug));
	}

	@Nullable
	public Object getCurrent(TriggerContext ctx) {
		return current.get(ctx);
	}

    /**
     * @return the expression whose values this loop is iterating over
     */
	public Expression<?> getLoopedExpression() {
		return expr;
  }
}
