package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.Time;

/**
 * This event will trigger each day at a given time.
 * Note that this may have some delay. The event certainly isn't accurate to the millisecond.
 *
 * @name At Time
 * @type EVENT
 * @pattern at %*time%
 * @since ALPHA
 * @author Mwexim
 */
public class EvtAtTime extends SkriptEvent {
    static {
        Parser.getMainRegistration()
                .newEvent(EvtAtTime.class, "*at %*time%")
                .setHandledContexts(AtTimeContext.class)
                .register();
    }

    private Literal<Time> time;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        time = (Literal<Time>) expressions[0];
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof AtTimeContext && time.getSingle().isPresent();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "at " + time.toString(ctx, debug);
    }

    public Literal<Time> getTime() {
        return time;
    }
}
