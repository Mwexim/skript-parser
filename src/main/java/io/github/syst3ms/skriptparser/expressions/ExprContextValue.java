package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValue;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValueState;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValues;

/**
 * A specific context value.
 * Refer to the documentation of the event to see which values can be used.
 *
 * @name Context Value
 * @pattern [the] [(past|previous)|(future|next)] [context-]<.+>
 * @since ALPHA
 * @author Mwexim
 */
public class ExprContextValue implements Expression<Object> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprContextValue.class,
				Object.class,
				false,
				"[the] (0:(past|previous)|1:|2:(future|next)) context-<.+>"
		);
	}

	private String name;
	private ContextValueState state;
	private ContextValue<?> value;

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, ParseContext parseContext) {
		name = parseContext.getMatches().get(0).group();
		state = ContextValueState.values()[parseContext.getParseMark()];

		for (Class<? extends TriggerContext> contextClass : parseContext.getParserState().getCurrentContexts()) {
			for (var contextValue : ContextValues.getContextValues(contextClass)) {
				if (contextValue.matches(name, state, false)) {
					value = contextValue;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return value.getType().getTypeClass();
	}

	@Override
	public boolean isSingle() {
		return value.isSingle();
	}

	@Override
	public Object[] getValues(TriggerContext ctx) {
		return value.getContextFunction().apply(ctx);
	}

	@Override
	public String toString(final TriggerContext ctx, final boolean debug) {
		return new String[] {"past ", "", "future "}[state.ordinal()] + "context-" + name;
	}
}
