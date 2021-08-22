package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;
import io.github.syst3ms.skriptparser.util.math.NumberMath;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A condition that randomly succeeds or fails, given the chance for it to do succeed.
 * Note that when the percent sign (%) is omitted, the chance is calculated from 0 to 1.
 *
 * @name Chance
 * @type SECTION
 * @pattern chance of %number%[\%]
 * @since ALPHA
 * @author Mwexim, Syst3ms
 */
@SuppressWarnings("unchecked")
public class SecChance extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
                SecChance.class,
                "chance of %number%[1:\\%]"
        );
    }

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    private Expression<Number> chance;
    private boolean percent;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        chance = (Expression<Number>) expressions[0];
        percent = parseContext.getNumericMark() == 1;
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        return chance.getSingle(ctx)
                .map(BigDecimalMath::getBigDecimal) // We use BigDecimal here not for its magnitude, but its precision
                .map(b -> b.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : b) // Coerce to 0 at least so that NumberMath#randomBigDecimal doesn't fail
                .map(b -> percent ? b.divide(BigDecimal.valueOf(100), BigDecimalMath.DEFAULT_ROUNDING_MODE) : b)
                .filter(b -> b.compareTo(NumberMath.randomBigDecimal(BigDecimal.ZERO, BigDecimal.ONE, random)) <= 0)
                .flatMap(b -> getFirst().map(s -> (Statement) s)) // Generic shenanigans
                .or(() -> getNext().map(s -> (Statement) s));
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "chance of " + chance.toString(ctx, debug) + (percent ? "%" : "");
    }
}
