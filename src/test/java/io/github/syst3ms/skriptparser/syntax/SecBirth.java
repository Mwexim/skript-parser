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

    private Expression<String> message;
    private SkriptLogger logger;

    private boolean dead;

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
        /*
         * This 'trapping' mechanism is done by design. We don't want 'birth' sections
         * to end early, since that is literally what they are supposed to do: discover
         * tests that are exiting too soon.
         */
        var item = getFirst();
        while (!item.equals(getNext())) {
            item = item.flatMap(val -> val.walk(ctx));
        }
        if (!dead) {
            SyntaxParserTest.addError(new SkriptRuntimeException(
                    message == null
                            ? "Birth section was not killed afterwards (" + logger.getFileName() + ")"
                            : message.getSingle(ctx).map(s -> (String) s).orElse(TypeManager.EMPTY_REPRESENTATION) + " (" + logger.getFileName() + ")"
            ));
        }
        dead = false;
        return getNext();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "birth";
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }
}