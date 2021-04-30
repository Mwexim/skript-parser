package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.classes.SkriptDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

/**
 * The object parsed as a given type.
 * Types like objects, strings and others that don't have an own parser cannot be parsed.
 * The date type is the only exception to this: Skript has a built-in parser for that.
 * Note that the {@code formatted as %string%} part is used to parse dates using a certain format
 * and will therefore not work on other types.
 * Learn <a href="https://docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html">here</a>
 * how to use these formats.
 *
 * @name Parse As
 * @type EXPRESSION
 * @pattern %string% parsed as %*type% [(using format|formatted as) %string%]
 * @since ALPHA
 * @author Mwexim
 */
public class ExprParseAs implements Expression<Object> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprParseAs.class,
				Object.class,
				true,
				"%string% parsed as %*type% [1:(using format|formatted as) %string%]"
				);
	}

	private Expression<String> expr;
	private Expression<Type<?>> type;
	private Class<?> parseTo;
	private boolean useFormat;
	private Expression<String> format;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		expr = (Expression<String>) expressions[0];
		type = (Expression<Type<?>>) expressions[1];
		parseTo = ((Literal<Type<?>>) expressions[1]).getSingle()
				.orElseThrow(AssertionError::new)
				.getTypeClass();
		if (parseTo == String.class) {
			parseContext.getLogger().error(
					"Parsing as string is redundant",
					ErrorType.SEMANTIC_ERROR,
					"Just remove the expression. There is no need to parse as a string when we already know the expression is a string"
			);
			return false;
		}
		useFormat = parseContext.getParseMark() == 1;
		if (useFormat)
			format = (Expression<String>) expressions[2];
		assert !useFormat || format != null;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getValues(TriggerContext ctx) {
		return expr.getSingle(ctx)
				.map(s -> {
					if (parseTo == SkriptDate.class) {
						SimpleDateFormat parseFormat = new SimpleDateFormat(
								useFormat ? ((Optional<String>) format.getSingle(ctx)).orElse(SkriptDate.DATE_FORMAT) : SkriptDate.DATE_FORMAT,
								SkriptDate.DATE_LOCALE
						);
						// We need to parse from the correct time zone.
						parseFormat.setTimeZone(TimeZone.getTimeZone(SkriptDate.getZoneId()));
						try {
							long timestamp = parseFormat.parse(s).getTime();
							return SkriptDate.of(timestamp);
						} catch (ParseException ex) {
							return null;
						}
					} else {
						return TypeManager.getByClass(parseTo)
								.map (t -> (Type<?>) t)
								.filter(t -> t.getLiteralParser().isPresent())
								.map(t -> t.getLiteralParser().get().apply(s))
								.orElse(null);
					}
				})
				.map(o -> new Object[] {o})
				.orElse(new Object[0]);
	}

	@Override
	public Class<?> getReturnType() {
		return parseTo;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return expr.toString(ctx, debug) + " parsed as " + type.toString(ctx, debug) + (useFormat ? " formatted as " + format.toString(ctx, debug) : "");
	}
}
