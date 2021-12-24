package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.Usage;
import io.github.syst3ms.skriptparser.syntax.TestContext.SubTestContext;

import java.time.Duration;

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
				.setHandledContexts(SubTestContext.class)
				.register();
		Parser.getMainRegistration()
				.newContextValue(SubTestContext.class, String.class, true, "test", __ -> new String[] {"Hello World!"})
				.setUsage(Usage.EXPRESSION_OR_ALONE)
				.register();
		Parser.getMainRegistration()
				.newContextValue(SubTestContext.class, String.class, false, "subclass", __ -> new String[] {"Hi", "Bye"})
				.setExcluded(SubTestContext.class)
				.register();
		Parser.getMainRegistration()
				.newContextValue(TestContext.class, String.class, true, "[some] pattern value", ctx -> new String[] {ctx.patternValue()})
				.setUsage(Usage.ALONE_ONLY)
				.setState(ContextValue.State.PAST)
				.register();
		Parser.getMainRegistration().addContextType(SubTestContext.class, Duration.class, SubTestContext::oneDay);
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
