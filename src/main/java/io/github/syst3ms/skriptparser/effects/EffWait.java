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
import java.util.Optional;

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
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Optional<? extends Duration> dur = duration.getSingle(ctx);
        if (dur.isEmpty())
            return getNext();
        if (getNext().isEmpty())
            return Optional.empty();
        ThreadUtils.runAfter(() -> Statement.runAll(getNext().get(), ctx), dur.get());
        return Optional.empty();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "wait " + duration.toString(ctx, debug);
    }
}
