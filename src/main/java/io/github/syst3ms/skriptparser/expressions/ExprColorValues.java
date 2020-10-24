package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.color.Color;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

/**
 * Certain color values of a given color.
 *
 * @name Date Values
 * @type EXPRESSION
 * @pattern [the] (0:rgb (value|color)|1:red [value]|2:green [value]|3:blue [value]) of %color%
 * @pattern %color%'[s] (0:rgb (value|color)|1:red [value]|2:green [value]|3:blue [value])
 * @pattern %color% as rgb
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
				"(0:rgb [(value|color)]|1:red value|2:green value|3:blue value)"
		);
	}

	int parseMark;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getParseMark();
		setOwner((Expression<Color>) expressions[0]);
		return true;
	}

	@Override
	public Optional<? extends Function<? super Color[], ? extends BigInteger[]>> getPropertyFunction() {
		return Optional.of(colors -> {
			Color c = colors[0];
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
				default:
					throw new IllegalStateException();
			}
		});
	}

	@Override
	public boolean isSingle() {
		return parseMark != 0;
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		switch (parseMark) {
			case 0:
				return getOwner().toString(ctx, debug) + " as rgb";
			case 1:
				return "red value of " + getOwner().toString(ctx, debug);
			case 2:
				return "green value of " + getOwner().toString(ctx, debug);
			case 3:
				return "blue value of " + getOwner().toString(ctx, debug);
			default:
				throw new IllegalStateException();
		}
	}
}
