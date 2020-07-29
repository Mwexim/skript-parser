package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * Length of a string.
 *
 * @name Length
 * @pattern length of %string%
 * @pattern %string%'s length
 * @since ALPHA
 * @author Romitou
 */
public class ExprLength implements Expression<Number> {

    private Expression<String> strValue;

    static {
        Main.getMainRegistration().addPropertyExpression(
                ExprLength.class,
                Number.class,
                true,
                "string",
                "length"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        strValue = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        String str = strValue.getSingle(ctx);
        if (str == null) return new Number[]{0};
        return new Number[]{str.length()};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "length of " + strValue.toString(ctx, debug);
    }

}
