package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParserTest;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.Nullable;

/**
 * Assert a condition. When false, this will throw an error.
 * Cannot be used outside of tests.
 *
 * @name Assert
 * @pattern assert %=boolean%
 * @since ALPHA
 * @author Mwexim
 */
public class EffAssert extends Effect {
	static {
		Parser.getMainRegistration().addEffect(
			EffAssert.class,
			"assert %=boolean% [with [message] %string%]"
		);
	}

	private Expression<Boolean> condition;
	private Expression<String> message;
	private SkriptLogger logger;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		condition = (Expression<Boolean>) expressions[0];
		if (expressions.length == 2)
			message = (Expression<String>) expressions[1];
		logger = parseContext.getLogger();
		return true;
	}

	@Override
	public void execute(TriggerContext ctx) {
		condition.getSingle(ctx)
				.filter(val -> (Boolean) !val)
				.ifPresent(__ -> SyntaxParserTest.addError(new AssertionError(
						message == null
								? "Assertion failed ('" + condition.toString(TriggerContext.DUMMY, logger.isDebug()) + "', " + logger.getFileName() + ")"
								: message.getSingle(ctx).map(s -> (String) s).orElse(TypeManager.EMPTY_REPRESENTATION) + " (" + logger.getFileName() + ")"
				)));
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "assert " + condition.toString(ctx, debug);
	}
}
