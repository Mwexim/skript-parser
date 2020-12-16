package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.parsing.SyntaxParserTest;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.Nullable;

/**
 * Assert a condition. When false, this will throw an error.
 * Cannot be used outside of tests.
 * The debug parse mark can be used to output some extra useful information if you
 * are stuck on a test, but should never be used in the final version.
 *
 * @name Assert
 * @pattern assert [1:with debug] %=boolean% [with [message] %string%]
 * @since ALPHA
 * @author Mwexim
 */
public class EffAssert extends Effect {
	static {
		Parser.getMainRegistration().addEffect(
			EffAssert.class,
			"assert [1:with debug] %=boolean% [with [message] %string%]",
				"throws [(error|exception)] <.+> [with [message] %string%]"
		);
	}

	private Expression<Boolean> condition;
	private Expression<String> message;
	private boolean debug;

	private String matched;
	@Nullable
	private Expression<?> throwsError;

	private int pattern;
	private SkriptLogger logger;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		logger = parseContext.getLogger();
		pattern = matchedPattern;
		if (pattern == 0) {
			condition = (Expression<Boolean>) expressions[0];
			if (expressions.length == 2)
				message = (Expression<String>) expressions[1];
			debug = parseContext.getParseMark() == 1;
		} else {
			matched = parseContext.getMatches().get(0).group();
			parseContext.getLogger().recurse();
			throwsError = SyntaxParser.parseExpression(matched, SyntaxParser.OBJECTS_PATTERN_TYPE,parseContext.getParserState(), parseContext.getLogger()).orElse(null);
			parseContext.getLogger().callback();
			if (expressions.length == 1)
				message = (Expression<String>) expressions[0];
		}
		return true;
	}

	@Override
	public void execute(TriggerContext ctx) {
		if (pattern == 0) {
			condition.getSingle(ctx)
					.filter(val -> !val.booleanValue())
					.ifPresent(__ -> SyntaxParserTest.addError(new AssertionError(errorString(ctx))));
		} else {
			if (throwsError != null)
				SyntaxParserTest.addError(new AssertionError(errorString(ctx)));
		}
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "assert " + condition.toString(ctx, debug);
	}

	private String errorString(TriggerContext ctx) {
		var error = new StringBuilder();
		if (this.message == null) {
			if (pattern == 0) {
				error.append("Assertion failed ('")
						.append(condition.toString(ctx, logger.isDebug()))
						.append("', ")
						.append(logger.getFileName())
						.append(")");
			} else {
				assert throwsError != null;
				error.append("Expected a non-parsable expression (")
						.append(matched)
						.append(", ")
						.append(logger.getFileName())
						.append(")");
			}
		} else {
			error.append(this.message.getSingle(ctx).map(s -> (String) s).orElse(TypeManager.EMPTY_REPRESENTATION))
					.append(" (")
					.append(logger.getFileName())
					.append(")");
		}
		if (debug) {
			error.append(" [")
					.append(condition.getClass().getSimpleName())
					.append("]");
		}
		return error.toString();
	}
}
