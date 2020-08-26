package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;

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
 * @author Mwexim
 */
@SuppressWarnings("unchecked")
public class SecChance extends CodeSection {

    static {
        Main.getMainRegistration().addSection(
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
        percent = parseContext.getParseMark() == 1;
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Optional<? extends Number> c = chance.getSingle(ctx);
        if (c.isEmpty())
            return getNext();
        double val = c.get().doubleValue();
        if (val < 0 || val > (percent ? 100 : 1))
            return getNext();
        BigDecimal randomNumber = (BigDecimal) NumberMath.random(BigDecimal.valueOf(0), BigDecimal.valueOf(100), false, random);

        // Tested with 1 000 000 iterations, average margin of error was 0.5%.
        if (randomNumber.doubleValue() <= (percent ? val : val * 100)) {
            return getFirst();
        } else {
            return getNext();
        }
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "chance of " + chance.toString(ctx, debug) + (percent ? "%" : "");
    }
}
