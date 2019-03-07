package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class InlineCondition extends Statement {
    private final Expression<Boolean> condition;

    public InlineCondition(Expression<Boolean> condition) {
        this.condition = condition;
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
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
