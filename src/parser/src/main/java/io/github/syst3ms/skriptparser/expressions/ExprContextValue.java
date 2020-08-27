package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValueTime;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValues;
import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValue;

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
		Main.getMainRegistration().addExpression(
			ExprContextValue.class,
			Object.class,
			false,
			3,
			"[the] [1:(past|previous)|2:(future|next)] context-<.+>"
		);
	}

	private String name;
	private ContextValueTime time;
	private ContextValue<?> value;

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, ParseContext parseContext) {
		name = parseContext.getMatches().get(0).group();
		switch (parseContext.getParseMark()) {
			case 1:
				time = ContextValueTime.PAST;
				break;
			case 2:
				time = ContextValueTime.FUTURE;
				break;
			default:
				time = ContextValueTime.PRESENT;
		}
		for (Class<? extends TriggerContext> ctx : parseContext.getParserState().getCurrentContexts()) {
			for (ContextValue<?> val : ContextValues.getContextValues()) {
				if (val.matches(ctx, name, time)) {
					value = val;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return value.getType();
	}

	@Override
	public Object[] getValues(TriggerContext ctx) {
		return value.getContextFunction().apply(ctx);
	}

	@Override
	public String toString(final @Nullable TriggerContext ctx, final boolean debug) {
		String state = "";
		if (time == ContextValueTime.PAST) {
			state = "past ";
		} else if (time == ContextValueTime.FUTURE) {
			state = "future ";
		}
		return state + "context-" + name;
	}

}
