package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Nullable;

public class EffPrintln extends Effect {
    private Expression<String> string;

    static {
        Main.getMainRegistration().addEffect(
            EffPrintln.class,
            "println %string%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        string = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        String str = string.getSingle(ctx);
        if (str == null)
            return;
        System.out.println(str);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "println " + string.toString(ctx, debug);
    }
}
