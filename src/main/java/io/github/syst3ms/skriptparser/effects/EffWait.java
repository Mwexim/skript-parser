package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.ThreadUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Waits a certain duration and then executes all the code after this effect.
 * Note that new events may be triggered during the wait time.
 *
 * @name Wait
 * @pattern (wait|halt) [for] %duration%
 * @since ALPHA
 * @author Mwexim
 */
public class EffWait extends Effect {

    static {
        Main.getMainRegistration().addEffect(
            EffWait.class,
            "(wait|halt) [for] %duration%"
        );
    }

    private Expression<Duration> duration;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        duration = (Expression<Duration>) expressions[0];
        return true;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement walk(TriggerContext ctx) {
        Duration dur = duration.getSingle(ctx);
        if (dur == null)
            return getNext();
        final Statement[] item = {getNext()};

        ThreadUtils.runAfter(() -> {
            while (!item[0].equals(item[0].getNext())) {
                item[0] = item[0].walk(ctx);
            }
        }, dur);
        return null;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "wait " + duration.toString(ctx, debug);
    }
}
