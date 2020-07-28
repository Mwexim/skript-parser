package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class ExprRandomNumber implements Expression<Number> {

    private Expression<Number> lowerNumber, maxNumber;

    static {
        Main.getMainRegistration().addExpression(
                ExprRandomNumber.class,
                Number.class,
                true,
                "test %number% to %number%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        System.out.println("yolo");
        lowerNumber = (Expression<Number>) expressions[0];
        maxNumber = (Expression<Number>) expressions[1];
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        System.out.println("test");
        Number low = lowerNumber.getSingle(ctx);
        Number max = maxNumber.getSingle(ctx).intValue();
        System.out.println("test 2");
        return new Integer[]{ThreadLocalRandom.current().nextInt(e, s + 1)};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "random number";
    }
}
