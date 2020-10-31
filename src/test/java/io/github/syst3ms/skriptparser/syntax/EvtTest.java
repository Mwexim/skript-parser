package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * The test event.
 * Cannot be used outside of tests.
 *
 * @name Test
 * @type EVENT
 * @pattern test
 * @since ALPHA
 * @author Mwexim
 */
public class EvtTest extends SkriptEvent {
    static {
        Parser.getMainRegistration()
                .newEvent(EvtTest.class, "*test")
                .setHandledContexts(TestContext.class)
                .register();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof TestContext;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "test";
    }
}
