package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * Prints some text to the console
 *
 * @name Print
 * @pattern print %strings% [to [the] console]
 * @since ALPHA
 * @author Syst3ms
 */
public class EffPrint extends Effect {
    private Expression<String> string;

    static {
        Main.getMainRegistration().addEffect(
            EffPrint.class,
            "print %strings% [to [the] console]"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        string = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        String[] strs = string.getValues(ctx);
        if (strs.length == 0)
            return;

        for (String str : strs) {
            System.out.println(str);
        }
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "println " + string.toString(ctx, debug);
    }
}
