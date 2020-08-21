package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

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

    private Expression<Number> chance;
    private boolean percent;

    @Override
    public void loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        super.loadSection(section, parserState, logger);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        chance = (Expression<Number>) expressions[0];
        percent = parseContext.getParseMark() == 1;
        return true;
    }

    @Override
    protected Statement walk(TriggerContext ctx) {
        Number c = chance.getSingle(ctx);
        if (c == null || Math.random() > (percent ? c.doubleValue() / 100 : c.doubleValue())) {
            return getNext();
        } else {
            return getFirst();
        }
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "chance of " + chance.toString(ctx, debug) + (percent ? "%" : "");
    }
}
