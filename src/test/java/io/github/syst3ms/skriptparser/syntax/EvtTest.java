package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * The test event.
 * Cannot be used outside of tests.
 *
 * @name Test
 * @type EVENT
 * @pattern test [[only] when %=boolean%]
 * @since ALPHA
 * @author Mwexim
 */
public class EvtTest extends SkriptEvent {
	static {
		Parser.getMainRegistration()
				.newEvent(EvtTest.class, "*test [[only] when %=boolean%]")
				.setHandledContexts(TestContext.RealTestContext.class)
				.addContextValue(TestContext.RealTestContext.class, String.class, true, "*test", __ -> new String[] {"Hello World!"})
				.register();
	}

	private Expression<Boolean> condition;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		if (expressions.length == 1)
			condition = (Expression<Boolean>) expressions[0];
		return true;
	}

	@Override
	public boolean check(TriggerContext ctx) {
		return ctx instanceof TestContext
			&& (condition == null || condition.getSingle(ctx).filter(Boolean::booleanValue).isPresent());
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "test";
	}
}
