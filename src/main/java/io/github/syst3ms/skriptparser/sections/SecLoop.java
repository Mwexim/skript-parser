package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * A section that iterates over a collection of elements
 */
public class SecLoop extends CodeSection {
	private Expression<?> expr;
	private final transient Map<TriggerContext, Object> current = new WeakHashMap<>();
	private final transient Map<TriggerContext, Iterator<?>> currentIter = new WeakHashMap<>();
	@Nullable
	private Statement actualNext;

	static {
		Main.getMainRegistration().addSection(
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
		return true;
	}

	@Override
	public void loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
		super.loadSection(section, parserState, logger);
		super.setNext(this);
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
		if (iter == null || !iter.hasNext()) {
			if (iter != null)
				currentIter.remove(ctx); // a loop inside another loop can be called multiple times in the same event
			return Optional.ofNullable(actualNext);
		} else {
			current.put(ctx, iter.next());
			return getFirst();
		}
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

	@Override
	public SecLoop setNext(@Nullable Statement next) {
		actualNext = next;
		return this;
	}

    /**
     * This method exists because Loop actually sets itself as its next element with {@link #getNext()}.
     * This way it has full control over when to stop iterating over
     * {@linkplain #getLoopedExpression() the looped expression}'s elements
     * @return the element that is actually after this Loop
     */
	@Nullable
	public Statement getActualNext() {
		return actualNext;
	}
}
