package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * The periodical event.
 * This will be triggered after each interval of a certain duration.
 * Note that when the duration is very precise, like milliseconds, it may be executed a bit later.
 * Large duration that go up to days are not recommended.
 *
 * @name Periodical
 * @type EVENT
 * @pattern every %duration%
 * @since ALPHA
 * @author Mwexim
 */
public class EvtPeriodical extends SkriptEvent {

    static {
        Main.getMainRegistration()
                .newEvent(EvtPeriodical.class, "*every %duration%")
                .setHandledContexts(PeriodicalContext.class)
                .register();
    }

    private Expression<Duration> duration;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        duration = (Expression<Duration>) expressions[0];
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof PeriodicalContext && duration.getSingle(ctx).isPresent();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "every " + duration.toString(ctx, debug);
    }

    public Expression<Duration> getDuration() {
        return duration;
    }
}
