package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.color.Color;

/**
 * A color specified by its hexadecimal value. One can use three types of hex values:
 * <ul>
 *     <li><b>3 hex digits:</b> each digit is doubled and the string is parsed as a 6-digit hex color.</li>
 *     <li><b>6 hex digits:</b> each pair of digits is respectively red, green and blue.</li>
 *     <li><b>8 hex digits:</b> same as a 6-digit hex color. The fourth pair is used for the alpha value.</li>
 * </ul>
 * A trailing hashtag (#) at the start of the string is allowed, but not necessary.
 *
 * @name Color from Hex
 * @type EXPRESSION
 * @pattern [the] color (from|of) [the] hex[adecimal] [value] %string%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprColorFromHex implements Expression<Color> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprColorFromHex.class,
				Color.class,
				true,
				"[the] color (from|of) [the] hex[adecimal] [value] %string%"
		);
	}

	private Expression<String> hex;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		hex = (Expression<String>) expressions[0];
		return true;
	}

	@Override
	public Color[] getValues(TriggerContext ctx) {
		return hex.getSingle(ctx)
				.flatMap(val -> Color.ofHex(val.startsWith("#") ? val.substring(1) : val))
				.map(val -> new Color[] {val})
				.orElse(new Color[0]);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "color from hex " + hex.toString(ctx, debug);
	}
}
