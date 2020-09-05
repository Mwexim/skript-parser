package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import org.jetbrains.annotations.Nullable;

public class LitDoubleSpecialValues implements Literal<Double> {
    private static final PatternInfos<Double> PATTERN_INFOS = new PatternInfos<>(new Object[][]{
            {"[positive] infinity", Double.POSITIVE_INFINITY},
            {"(negative |-)infinity", Double.NEGATIVE_INFINITY},
            {"nan", Double.NaN}
            /*
             * I'll only use a simple "nan" syntax because "not a number" could cause very weird and confusing behavior,
             * especially with CondExprCompare
             */
    });
    private int pattern;

    static {
        Parser.getMainRegistration().addExpression(
                LitDoubleSpecialValues.class,
                Double.class,
                true,
                PATTERN_INFOS.getPatterns()
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        pattern = matchedPattern;
        return true;
    }

    @Override
    public Double[] getValues() {
        return new Double[]{PATTERN_INFOS.getInfo(pattern)};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        switch (pattern) {
            case 0:
                return "Infinity";
            case 1:
                return "-Infinity";
            case 2:
                return "NaN";
            default:
                throw new IllegalStateException();
        }
    }
}
