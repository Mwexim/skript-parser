package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class ExprRandomNumber implements Expression<Integer> {

    private Expression<Integer> startNumber;
    private Expression<Integer> endNumber;

    static {
        Main.getMainRegistration().addExpression(
                ExprRandomNumber.class,
                Integer.class,
                true,
                "test %number% to %number%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        System.out.println("yolo");
        startNumber = (Expression<Integer>) expressions[0];
        endNumber = (Expression<Integer>) expressions[1];
        return true;
    }

    @Override
    public Integer[] getValues(TriggerContext ctx) {
        System.out.println("test");
        int e = startNumber.getSingle(ctx).intValue();
        int s = endNumber.getSingle(ctx).intValue();
        System.out.println("test 2");
        return new Integer[]{ThreadLocalRandom.current().nextInt(e, s + 1)};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "random number";
    }
}
