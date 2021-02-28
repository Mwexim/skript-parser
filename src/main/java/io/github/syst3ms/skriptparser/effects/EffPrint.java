package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.StringUtils;

/**
 * Prints some text to the console
 *
 * @name Print
 * @pattern print %strings% [to [the] console]
 * @since ALPHA
 * @author Syst3ms
 */
public class EffPrint extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
            EffPrint.class,
            "print %strings% [to [the] console]"
        );
    }

    private Expression<String> string;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        string = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        String[] strs = StringUtils.applyTags(string, ctx, "console");
        if (strs.length == 0)
            return;

        for (String str : strs) {
            System.out.println(str);
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "print " + string.toString(ctx, debug);
    }
}
