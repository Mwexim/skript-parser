package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * This event will check against a certain condition and will trigger when the condition is met.
 * Note that this condition is checked each tick, which is defined as 50ms.
 *
 * @name When
 * @type EVENT
 * @pattern when %=boolean%
 * @since ALPHA
 * @author Mwexim
 */
public class EvtWhen extends SkriptEvent {
    static {
        Parser.getMainRegistration()
                .newEvent(EvtWhen.class, "*when %=boolean%")
                .setHandledContexts(WhenContext.class)
                .register();
    }

    private Expression<Boolean> condition;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (condition.getSingle(ctx).isEmpty())
            return false;
        return ctx instanceof WhenContext && condition.getSingle(ctx).get();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "when " + condition.toString(ctx, debug);
    }
}
