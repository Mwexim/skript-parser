package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.color.Color;

import java.math.BigInteger;

/**
 * Certain color values of a given color.
 *
 * @name Date Values
 * @type EXPRESSION
 * @pattern [the] (hex[adecimal]|red|green|blue|alpha) value of %color%
 * @pattern %color%'[s] (hex[adecimal]|red|green|blue|alpha)
 * @since ALPHA
 * @author Mwexim
 */
public class ExprColorValues extends PropertyExpression<Color, Object> {
	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprColorValues.class,
				Object.class,
				"(0:hex[adecimal]|1:red|2:green|3:blue|4:alpha) value",
				"colors"
		);
	}

	private int mark;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		mark = parseContext.getNumericMark();
		return super.init(expressions, matchedPattern, parseContext);
	}

	@Override
	public Object getProperty(Color owner) {
		switch (mark) {
			case 0:
				return owner.getHex();
			case 1:
				return BigInteger.valueOf(owner.getRed());
			case 2:
				return BigInteger.valueOf(owner.getGreen());
			case 3:
				return BigInteger.valueOf(owner.getBlue());
			case 4:
				return BigInteger.valueOf(owner.getAlpha());
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public Class<?> getReturnType() {
		return mark == 0 ? String.class : BigInteger.class;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return toString(ctx, debug, new String[] {"hex", "red", "green", "blue", "alpha"}[mark] + " value");
	}
}
