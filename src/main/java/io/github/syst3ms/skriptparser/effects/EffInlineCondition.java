package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * Runs the next line of this section if a condition is met.
 * Note that when the condition is not met, it will still run the
 * next code outside this section if possible.
 *
 * @name Inline Condition
 * @pattern continue [only] if %=boolean%
 * @since ALPHA
 * @author Mwexim
 */
public class EffInlineCondition extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
            EffInlineCondition.class,
            5,
            "continue [only] if %=boolean%"
        );
    }

    private Expression<Boolean> condition;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean run(TriggerContext ctx) {
        return condition.getSingle(ctx).filter(b -> b).isPresent();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "continue if " + condition.toString(ctx, debug);
    }
}
