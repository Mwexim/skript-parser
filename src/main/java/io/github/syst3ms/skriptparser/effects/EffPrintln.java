package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
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
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        string = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public void execute(Event e) {
        String str = string.getSingle(e);
        if (str == null)
            return;
        System.out.println(str);
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "println " + string.toString(e, debug);
    }
}
