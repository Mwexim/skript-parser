package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A section that iterates over a collection of elements
 */
public class Loop extends CodeSection {
	private Expression<?> expr;
	private transient Map<TriggerContext, Object> current = new WeakHashMap<>();
	private transient Map<TriggerContext, Iterator<?>> currentIter = new WeakHashMap<>();
	@Nullable
	private Effect actualNext;

	static {
		Main.getMainRegistration().addSection(
			Loop.class,
			"loop %objects%"
		);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		expr = expressions[0];
		if (expr.isSingle()) {
		    // REMIND error
			return false;
		}
		return true;
	}

	@Override
	public void loadSection(FileSection section) {
		ScriptLoader.addCurrentLoop(this);
		setItems(ScriptLoader.loadItems(section));
		ScriptLoader.removeCurrentLoop();
		super.setNext(this);
	}

	@Override
	@Nullable
	protected Effect walk(TriggerContext e) {
		Iterator<?> iter = currentIter.get(e);
		if (iter == null) {
			iter = expr instanceof Variable ? ((Variable<?>) expr).variablesIterator(e) : expr.iterator(e);
			if (iter != null) {
				if (iter.hasNext())
					currentIter.put(e, iter);
				else
					iter = null;
			}
		}
		if (iter == null || !iter.hasNext()) {
			if (iter != null)
				currentIter.remove(e); // a loop inside another loop can be called multiple times in the same event
			return actualNext;
		} else {
			current.put(e, iter.next());
			return getFirst();
		}
	}

	@Override
	public String toString(@Nullable TriggerContext e, boolean debug) {
		return "loop " + expr.toString(e, debug);
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
	public Loop setNext(@Nullable Effect next) {
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
	public Effect getActualNext() {
		return actualNext;
	}
}
