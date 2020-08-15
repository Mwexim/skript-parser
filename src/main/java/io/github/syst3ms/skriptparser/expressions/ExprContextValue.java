package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.ContextValueManager;
import org.jetbrains.annotations.Nullable;

import static io.github.syst3ms.skriptparser.registration.ContextValueManager.ContextValue;

/**
 * A specific context value.
 * Refer to the documentation of the event to see which values can be used.
 *
 * @name Context Value
 * @pattern [the] (past|future|) context-<.+>
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
			"[the] (1:past|2:future|3:) context-<.+>"
		);
	}

	private String name;
	private int timeline;
	private ContextValue<?> value;

	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, ParseContext parseContext) {
		name = parseContext.getMatches().get(0).group();
		if (parseContext.getParseMark() == 1) timeline = -1;
		else if (parseContext.getParseMark() == 2) timeline = 1;
		else timeline = 0;
		for (Class<? extends TriggerContext> ctx : parseContext.getParserState().getCurrentContexts())
			for (ContextValue<?> val : ContextValueManager.getContextValues())
				if (val.matches(ctx, name, timeline)) {
					value = val;
					return true;

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
		if (timeline == -1)
			state = "past ";
		else if (timeline == 1)
			state = "future ";
		return state + "context-" + name;
	}

}
