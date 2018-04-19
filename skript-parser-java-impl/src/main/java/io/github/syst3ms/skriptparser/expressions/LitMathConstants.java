package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class LitMathConstants implements Expression<Number> {
    private static final BigDecimal PI = new BigDecimal("3.14159265358979323846264338327950288419716939937510582097494459230781640628620");
    private static final BigDecimal E = new BigDecimal("2.71828182845904523536028747135266249775724709369995957496696762772407663035354");
    private static final BigDecimal PHI = new BigDecimal("1.6180339887498948482045868343656381177203091798057628621354486227052604628189");
    private int pattern;

    static {
        Main.getMainRegistration().addExpression(
                LitMathConstants.class,
                Number.class,
                true,
                "(0¦pi|1¦e|2¦phi)"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        pattern = parseResult.getParseMark();
        return true;
    }

    @Override
    public Number[] getValues(Event e) {
        if (pattern == 0) {
            return new Number[]{PI};
        } else if (pattern == 1) {
            return new Number[]{E};
        } else if (pattern == 2) {
            return new Number[]{PHI};
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        if (pattern == 0) {
            return "pi";
        } else if (pattern == 1) {
            return "e";
        } else if (pattern == 2) {
            return "phi";
        } else {
            throw new IllegalStateException();
        }
    }
}
