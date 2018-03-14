package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.SkriptLogger;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class Loop extends CodeSection {
	private Expression<?> expr;
	private transient Map<Event, Object> current = new WeakHashMap<>();
	private transient Map<Event, Iterator<?>> currentIter = new WeakHashMap<>();
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
			SkriptLogger.error("Can't loop a single expression !");
			return false;
		}
		return true;
	}

	@Override
	public void loadSection(FileSection section) {
		ScriptLoader.addCurrentLoop(this);
		setTriggerItems(ScriptLoader.loadItems(section));
		ScriptLoader.removeCurrentLoop();
		super.setNext(this);
	}

	@Override
	@Nullable
	protected Effect walk(Event e) {
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
	public String toString(@Nullable Event e, boolean debug) {
		return "loop " + expr.toString(e, debug);
	}

	@Nullable
	public Object getCurrent(Event e) {
		return current.get(e);
	}

	public Expression<?> getLoopedExpression() {
		return expr;
	}

	@Override
	public Loop setNext(@Nullable Effect next) {
		actualNext = next;
		return this;
	}

	@Nullable
	public Effect getActualNext() {
		return actualNext;
	}
}
