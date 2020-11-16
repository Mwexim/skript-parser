package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.parsing.SyntaxParserTest;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Whenever a birth section is called, one of the methods inside must be {@link EffDeath}.
 * If not, an error will be thrown and the test will fail.
 * Use this to make sure a script reaches a certain line of code.
 * Cannot be used outside of tests.
 *
 * @name Birth
 * @type SECTION
 * @pattern birth
 * @since ALPHA
 * @author Mwexim
 */
public class SecBirth extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
            SecBirth.class,
            "birth"
        );
    }

    private static final List<EffDeath> currentDeaths = new ArrayList<>();

    private SkriptLogger logger;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        logger = parseContext.getLogger();
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        var item = getFirst();
        while (!item.equals(getNext())) {
            item = item.flatMap(val -> val.walk(ctx));
        }
        if (currentDeaths.isEmpty()) {
            SyntaxParserTest.addError(new SkriptRuntimeException(
                "Birth section was not killed afterwards ("
                + logger.getFileName() + ")"
            ));
        }
        currentDeaths.clear();
        return getNext();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "birth";
    }

    public static void addDeath(EffDeath death) {
        currentDeaths.add(death);
    }

    public static void removeDeath(EffDeath death) {
        currentDeaths.remove(death);
    }
}