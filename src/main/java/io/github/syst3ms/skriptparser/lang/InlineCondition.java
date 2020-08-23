package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A condition that is executed as a single line.
 * Useful to make quick assertions without having to make sections for each one.
 * @see Conditional
 */
public class InlineCondition extends Statement {

    private final Expression<Boolean> condition;

    public InlineCondition(Expression<Boolean> condition) {
        this.condition = condition;
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean run(TriggerContext ctx) {
        Boolean cond = condition.getSingle(ctx);
        return cond != null && cond;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return condition.toString(ctx, debug);
    }
}
