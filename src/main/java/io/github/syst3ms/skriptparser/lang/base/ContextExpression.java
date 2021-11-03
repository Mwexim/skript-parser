package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValue;
import org.jetbrains.annotations.Contract;

/**
 * An {@link Expression} that corresponds to a contextual value. Each trigger
 * can carry multiple instances of data (called 'context values'). This expression
 * holds a reference to such value.
 * @param <C> the TriggerContext class
 * @param <T> the Expression's type
 * @author Mwexim
 */
public class ContextExpression<C extends TriggerContext, T> implements Expression<T> {
	private final ContextValue<C, T> info;
	private final String value;
	private final boolean standalone;

	public ContextExpression(ContextValue<C, T> info, String value, boolean standalone) {
		this.info = info;
		this.value = value;
		this.standalone = standalone;
	}

	@Override
	@Contract("_, _, _ -> fail")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		throw new UnsupportedOperationException();
	}

	// TODO instanceof check
	@SuppressWarnings("unchecked")
	@Override
	public T[] getValues(TriggerContext ctx) {
		return info.getFunction().apply((C) ctx);
	}

	@Override
	public boolean isSingle() {
		return info.getReturnType().isSingle();
	}

	@Override
	public Class<? extends T> getReturnType() {
		return info.getReturnType().getType().getTypeClass();
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return new String[] {"past ", "", "future "}[info.getState().ordinal()] + (standalone ? "" : "context-") + value;
	}
}