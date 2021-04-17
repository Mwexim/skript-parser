package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.regex.Pattern;

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
 * @pattern replace [(all|every|[the] first|[the] last|[the] %integer%(st|nd|rd|th))] %strings% in[side] %strings% with %string%
 * @pattern replace [(all|every|[the] first|[the] last|[the] %integer%(st|nd|rd|th))] %strings% with %string% in[side] %strings%
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
				"replace [(0:all|0:every|1:[the] first|2:[the] last|3:[the] %integer%(st|nd|rd|th))] %strings% in[side] %strings% with %string%",
				"replace [(0:all|0:every|1:[the] first|2:[the] last|3:[the] %integer%(st|nd|rd|th))] %strings% with %string% in[side] %strings%"
		);
	}

	// 0 = all, 1 = first, 2 = last, 3 = indexed
	private int type;
	@Nullable
	private Expression<BigInteger> index;
	private Expression<String> toMatch;
	private Expression<String> toReplace;
	private Expression<String> replacement;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		type = parseContext.getParseMark();
		switch (type) {
			case 0:
			case 1:
			case 2:
				toMatch = (Expression<String>) expressions[0];
				toReplace = (Expression<String>) expressions[1 + matchedPattern];
				replacement = (Expression<String>) expressions[2 - matchedPattern];
				break;
			case 3:
				index = (Expression<BigInteger>) expressions[0];
				toMatch = (Expression<String>) expressions[1];
				toReplace = (Expression<String>) expressions[2 + matchedPattern];
				replacement = (Expression<String>) expressions[3 - matchedPattern];
				break;
			default:
				throw new IllegalStateException();
		}
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
		} else if (type == 4) {
			assert index != null;
			if (index.getSingle(ctx).orElse(null) == null)
				return replacedValues;
		}

		for (int i = 0; i < replacedValues.length; i++) {
			for (String match : matchedValues) {
				String replaced;
				switch (type) {
					case 0:
						replaced = replacedValues[i].replace(match, replacementValue);
						break;
					case 1:
						replaced = replacedValues[i].replaceFirst(Pattern.quote(match), replacementValue);
						break;
					case 2:
						int lastIndex = replacedValues[i].lastIndexOf(match);
						if (lastIndex < 0 || lastIndex >= replacedValues[i].length())
							continue;

						int limitIndex = lastIndex + replacedValues[i].length();
						replaced = replacedValues[i].substring(0, lastIndex)
								+ replacementValue
								+ (limitIndex < replacedValues[i].length() ? replacedValues[i].substring(limitIndex) : "");
						break;
					case 3:
						assert index != null;
						int index = this.index.getSingle(ctx).map(BigInteger::intValue).orElseThrow(AssertionError::new);
						if (index < 0 || index >= replacedValues[i].length())
							continue;

						limitIndex = index + replacedValues[i].length();
						replaced = replacedValues[i].substring(0, index)
								+ replacementValue
								+ (limitIndex < replacedValues[i].length() ? replacedValues[i].substring(index + replacedValues[i].length()) : "");
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
		String typeString;
		switch (type) {
			case 0:
				typeString = "all ";
				break;
			case 1:
				typeString = "first ";
				break;
			case 2:
				typeString = "last ";
				break;
			case 3:
				assert index != null;
				typeString = "occurrence number " + index.toString(ctx, debug) + " ";
				break;
			default:
				throw new IllegalStateException();
		}
		return "replace " + typeString + toMatch.toString(ctx, debug)
				+ " in " + toReplace.toString(ctx, debug)
				+ " with " + replacement.toString(ctx, debug);
	}
}
