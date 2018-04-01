package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Nullable;

public class ExprWhether implements Expression<Boolean> {
    private Expression<Boolean> condition;

    static {
        Main.getMainRegistration().addExpression(
                ExprWhether.class,
                Boolean.class,
                true,
                "whether %~=boolean%"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    public Boolean[] getValues(Event e) {
        return condition.getValues(e);
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "whether " + condition.toString(e, debug);
    }
}
