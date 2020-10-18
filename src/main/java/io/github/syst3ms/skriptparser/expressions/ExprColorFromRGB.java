package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.color.Color;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/**
 * A color specified by its RGB value.
 * Each value, this means red, green and blue, can be a range from 0 to 255.
 * Any values outside of those ranges will result in no color to be created.
 *
 * @name Color from RGB
 * @type EXPRESSION
 * @pattern [the] color (from|of) [the] rgb [value] %integer%, %integer%(,| and) %integer%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprColorFromRGB implements Expression<Color> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprColorFromRGB.class,
				Color.class,
				true,
				"[the] color (from|of) [the] rgb [value] %integer%, %integer%(,| and) %integer%"
		);
	}

	private Expression<BigInteger> red, green, blue;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		red = (Expression<BigInteger>) expressions[0];
		green = (Expression<BigInteger>) expressions[1];
		blue = (Expression<BigInteger>) expressions[2];
		return true;
	}

	@Override
	public Color[] getValues(TriggerContext ctx) {
		int r = red.getSingle(ctx).map(BigInteger::intValue).orElse(-1);
		int g = green.getSingle(ctx).map(BigInteger::intValue).orElse(-1);
		int b = blue.getSingle(ctx).map(BigInteger::intValue).orElse(-1);
		if (0 <= r && r < 256
				&& 0 <= g && g < 256
				&& 0 <= b && b < 256)
			return new Color[] {Color.of((byte) r, (byte) g, (byte) b)};
		return new Color[0];
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "color from rgb " + red.toString(ctx, debug) + ", " + green.toString(ctx, debug) + ", " + blue.toString(ctx, debug);
	}
}
