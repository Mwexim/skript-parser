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
import io.github.syst3ms.skriptparser.types.TypeManager;
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
 * @pattern birth [with [message] %string%]
 * @since ALPHA
 * @author Mwexim
 */
public class SecBirth extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
            SecBirth.class,
            "birth [with [message] %string%]"
        );
    }

    private static final List<EffDeath> currentDeaths = new ArrayList<>();

    private Expression<String> message;
    private SkriptLogger logger;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        logger = parseContext.getLogger();
        if (expressions.length == 1)
            message = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        var item = getFirst();
        while (!item.equals(getNext())) {
            item = item.flatMap(val -> val.walk(ctx, true));
        }
        if (currentDeaths.isEmpty()) {
            SyntaxParserTest.addError(new SkriptRuntimeException(
                    message == null
                            ? "Birth section was not killed afterwards (" + logger.getFileName() + ")"
                            : message.getSingle(ctx).map(s -> (String) s).orElse(TypeManager.EMPTY_REPRESENTATION) + " (" + logger.getFileName() + ")"
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