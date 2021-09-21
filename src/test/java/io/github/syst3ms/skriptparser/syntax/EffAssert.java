package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.parsing.SyntaxParserTest;
import org.jetbrains.annotations.Nullable;

/**
 * Assert a condition. When false, this will throw an error.
 * Cannot be used outside of tests.
 *
 * @name Assert
 * @pattern assert %=boolean% [with [message] %string%]
 * @since ALPHA
 * @author Mwexim
 */
public class EffAssert extends Effect {
	static {
		Parser.getMainRegistration().addEffect(
				EffAssert.class,
				"assert %=boolean% [with [message] %string%]",
				"(0:throws [(error|exception)]|1:compiles) <.+> [with [message] %string%]"
		);
	}

	private Expression<Boolean> condition;
	@Nullable
	private Expression<String> message;

	private String matched;
	private boolean shouldThrow;
	private boolean compiled;

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
		} else {
			matched = parseContext.getMatches().get(0).group();
			shouldThrow = parseContext.getNumericMark() == 0;
			parseContext.getLogger().recurse();
			compiled = SyntaxParser.parseExpression(matched, SyntaxParser.OBJECTS_PATTERN_TYPE, parseContext.getParserState(), parseContext.getLogger()).isPresent();
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
			if (shouldThrow && compiled)
				SyntaxParserTest.addError(new AssertionError(errorString(ctx)));
		}
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		var message = this.message != null ? " " + this.message.toString(ctx, debug) : "";
		if (pattern == 0) {
			return "assert " + condition.toString(ctx, debug) + message;
		} else {
			return shouldThrow ? "throws " + matched + message : "compiles " + matched + message;
		}
	}

	private String errorString(TriggerContext ctx) {
		if (this.message == null) {
			if (pattern == 0) {
				return "Assertion failed ('" + condition.toString(ctx, logger.isDebug()) + "', " + logger.getFileName() + ")";
			} else {
				if (shouldThrow) {
					assert compiled;
					return "Expected a non-parsable expression (" + matched + ", " + logger.getFileName() + ")";
				} else {
					assert !compiled;
					return "Expected a parsable expression (" + matched + ", " + logger.getFileName() + ")";
				}
			}
		} else {
			return message.getSingle(ctx).map(String::new).orElse("No message provided") + " (" + logger.getFileName() + ")";
		}
	}
}
