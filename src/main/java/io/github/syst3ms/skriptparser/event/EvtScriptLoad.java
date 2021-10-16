package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * The script loading event.
 *
 * @name On Script Load
 * @type EVENT
 * @pattern [on] script load[ing]
 * @since ALPHA
 * @context arguments
 * @author Syst3ms
 */
public class EvtScriptLoad extends SkriptEvent {
    static {
        Parser.getMainRegistration()
                .newEvent(EvtScriptLoad.class, "script load[ing]")
                .setHandledContexts(ScriptLoadContext.class)
                .register();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return ctx instanceof ScriptLoadContext;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "script loading";
    }
}
