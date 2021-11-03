package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Contract;

/**
 * A tagged expression is a string expression that contains tags.
 * Tags are small structures of code that can affect a certain part of the string easily
 * by changing it accordingly. Default, these tags are not parsed unless a
 * {@link ContextExpression} is used.
 */
public class ContextExpression<C extends TriggerContext, T> implements Expression<T> {
	private final ContextValueInfo<C, T> info;
	private final String value;
	private final boolean standalone;

	public ContextExpression(ContextValueInfo<C, T> info, String value, boolean standalone) {
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
		return new String[] {"past ", "", "future "}[info.getState().ordinal()]
				+ (standalone ? "" : "context-")
				+ value;
	}
}
