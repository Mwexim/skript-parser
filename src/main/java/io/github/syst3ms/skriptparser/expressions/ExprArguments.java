package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.ScriptLoadContext;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ExprArguments implements Expression<String> {

    static {
        Main.getMainRegistration().addExpression(
                ExprArguments.class,
                String.class,
                false,
                "[the] [main] arguments"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (!Arrays.asList(parseContext.getCurrentContexts()).contains(ScriptLoadContext.class)) {
            parseContext.getLogger().error("Can't access the program arguments outside of the script load event !", ErrorType.SEMANTIC_ERROR);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String[] getValues(TriggerContext ctx) {
        assert ctx instanceof ScriptLoadContext;
        return ((ScriptLoadContext) ctx).getArguments();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "main arguments";
    }
}
