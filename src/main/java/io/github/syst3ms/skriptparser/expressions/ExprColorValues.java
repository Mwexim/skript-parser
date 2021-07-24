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
 * @pattern [the] (0:rgb (value|color)|1:red [value]|2:green [value]|3:blue [value]) of %color%
 * @pattern %color%'[s] (0:rgb (value|color)|1:red [value]|2:green [value]|3:blue [value])
 * @since ALPHA
 * @author Mwexim
 */
public class ExprColorValues extends PropertyExpression<BigInteger, Color> {
	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprColorValues.class,
				BigInteger.class,
				false,
				"color",
				"(rgb[4:a] [value[s]]|1:red value|2:green value|3:blue value)"
		);
	}

	private int parseMark;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getNumericMark();
		setOwner((Expression<Color>) expressions[0]);
		return true;
	}

	@Override
	public BigInteger[] getProperty(Color[] owners) {
		Color c = owners[0];
		switch (parseMark) {
			case 0:
				return new BigInteger[] {
						BigInteger.valueOf(c.getRed()),
						BigInteger.valueOf(c.getGreen()),
						BigInteger.valueOf(c.getBlue())
				};
			case 1:
				return new BigInteger[] {BigInteger.valueOf(c.getRed())};
			case 2:
				return new BigInteger[] {BigInteger.valueOf(c.getGreen())};
			case 3:
				return new BigInteger[] {BigInteger.valueOf(c.getBlue())};
			case 4:
				return new BigInteger[] {
						BigInteger.valueOf(c.getRed()),
						BigInteger.valueOf(c.getGreen()),
						BigInteger.valueOf(c.getBlue()),
						BigInteger.valueOf(c.getAlpha())
				};
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public boolean isSingle() {
		return parseMark != 0 && parseMark != 4;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		switch (parseMark) {
			case 0:
				return "rgb value of " + getOwner().toString(ctx, debug);
			case 1:
				return "red value of " + getOwner().toString(ctx, debug);
			case 2:
				return "green value of " + getOwner().toString(ctx, debug);
			case 3:
				return "blue value of " + getOwner().toString(ctx, debug);
			case 4:
				return "rgba value of " + getOwner().toString(ctx, debug);
			default:
				throw new IllegalStateException();
		}
	}
}
