package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.lambda.ArgumentSection;
import io.github.syst3ms.skriptparser.lang.lambda.SkriptConsumer;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * A section that iterates over a collection of elements
 */
public class SecLoop extends ArgumentSection {
	private Expression<?> expr;
	private final transient Map<TriggerContext, Object> current = new WeakHashMap<>();
	private final transient Map<TriggerContext, Iterator<?>> currentIter = new WeakHashMap<>();
	private SkriptConsumer<SecLoop> lambda;

	static {
		Parser.getMainRegistration().addSection(
			SecLoop.class,
			"loop %objects%"
		);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		expr = expressions[0];
		if (expr.isSingle()) {
		    parseContext.getLogger().error("Cannot loop a single value", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		lambda = SkriptConsumer.create(this);
		return true;
	}

	@Override
	public Optional<? extends Statement> walk(TriggerContext ctx) {
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
		return "loop " + expr.toString(ctx, debug);
	}

	@Nullable
	public Object getCurrent(TriggerContext e) {
		return current.get(e);
	}

    /**
     * @return the expression whose values this loop is iterating over
     */
	public Expression<?> getLoopedExpression() {
		return expr;
	}
}
