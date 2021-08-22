package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.TaggedExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

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

    private Expression<String> expression;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expression = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        for (String val : TaggedExpression.apply(expression, ctx, "console")) {
            System.out.println(val);
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "print " + expression.toString(ctx, debug);
    }
}
