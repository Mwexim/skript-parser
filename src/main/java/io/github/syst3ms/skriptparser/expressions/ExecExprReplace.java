package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Replaces certain occurrences in a string with another string and returns those applied strings.
 * If used like an effect, instead of returning the applied strings, specified above, it will
 * <b>replace</b> them from in the list. Note that not all expressions support this behaviour.
 * If no valid match was found, nothing will happen.
 * <br>
 * Note that indices in Skript start at one.
 *
 * @name Replace
 * @type EFFECT/EXPRESSION
 * @pattern replace [(all|every|[the] first|[the] last)] [regex [pattern[s]]] %strings% in %strings% with %string%
 * @pattern replace [(all|every|[the] first|[the] last)] [regex [pattern[s]]] %strings% with %string% in %strings%
 * @since ALPHA
 * @author Mwexim
 * @see ExprElement
 */
public class ExecExprReplace extends ExecutableExpression<String> {
	static {
		Parser.getMainRegistration().addSelfRegisteringElement(
				ExecExprReplace.class,
				String.class,
				false,
				"replace [(0:all|0:every|1:[the] first|2:[the] last)] [:regex [pattern[s]]] %strings% in %strings% with %string%",
				"replace [(0:all|0:every|1:[the] first|2:[the] last)] [:regex [pattern[s]]] %strings% with %string% in %strings%"
		);
	}

	private Expression<String> toMatch;
	private Expression<String> toReplace;
	private Expression<String> replacement;

	private int type;
	private boolean regex;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		type = parseContext.getNumericMark();
		regex = parseContext.hasMark("regex");

		toMatch = (Expression<String>) expressions[0];
		toReplace = (Expression<String>) expressions[1 + matchedPattern];
		replacement = (Expression<String>) expressions[2 - matchedPattern];

		if (!toReplace.acceptsChange(ChangeMode.SET, String[].class)) {
			var logger = parseContext.getLogger();
			logger.error(
					"The expression '"
							+ toReplace.toString(TriggerContext.DUMMY, logger.isDebug())
							+ "' cannot be changed",
					ErrorType.SEMANTIC_ERROR
			);
			return false;
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx, boolean isEffect) {
		String[] replacedValues = toReplace.getValues(ctx);
		String[] matchedValues = toMatch.getValues(ctx);
		String replacementValue = replacement.getSingle(ctx).orElse(null);
		if (replacementValue == null) {
			return replacedValues;
		}

		for (int i = 0; i < replacedValues.length; i++) {
			for (String match : matchedValues) {
				String current = replacedValues[i];
				String replaced;
				if (regex) {
					// The regex pattern must be valid
					try {
						Pattern.compile(match);
					} catch (PatternSyntaxException ignored) {
						continue;
					}
				}
				switch (type) {
					case 0:
						// All occurrences
						replaced = regex
								? current.replaceAll(match, replacementValue)
								: current.replace(match, replacementValue);
						break;
					case 1:
						// First occurrence
						replaced = current.replaceFirst(
								regex ? match : Pattern.quote(match),
								replacementValue
						);
						break;
					case 2:
						// Last occurrence
						// This regex pattern flushes away as many characters as it can, leaving the last occurrence.
						Matcher matcher = Pattern.compile(".*(" + match + ")").matcher(current);
						if (regex && !matcher.matches())
							continue;

						int lastIndex = regex
								? matcher.start(1)
								: current.lastIndexOf(match);
						if (lastIndex < 0 || lastIndex >= current.length())
							continue;

						int limitIndex = lastIndex + (regex
								? matcher.group(1).length()
								: match.length());
						replaced = current.substring(0, lastIndex)
								+ replacementValue
								+ (limitIndex < current.length() ? current.substring(limitIndex) : "");
						break;
					default:
						throw new IllegalStateException();
				}
				replacedValues[i] = replaced;
			}
		}
		if (isEffect)
			toReplace.change(ctx, replacedValues, ChangeMode.SET);
		return replacedValues;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "replace "
				+ new String[] {"all ", "first ", "last "}[type] + toMatch.toString(ctx, debug)
				+ " in " + toReplace.toString(ctx, debug)
				+ " with " + replacement.toString(ctx, debug);
	}
}
