package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class ExprUnaryMathFunctions implements Expression<Number> {
    public static final PatternInfos<UnaryOperator<Number>> PATTERNS = new PatternInfos<>(new Object[][]{
            {"abs[olute value of] %number%", (UnaryOperator<Number>) NumberMath::abs},
            {"(%number%!|factorial of %number%)", (UnaryOperator<Number>) NumberMath::factorial},
            {"(sqrt|square root of) %number%", (UnaryOperator<Number>) NumberMath::sqrt},
            {"floor[ed] %number%", (UnaryOperator<Number>) NumberMath::floor},
            {"ceil[ing|ed] %number%", (UnaryOperator<Number>) NumberMath::ceil},
            {"round[ed] %number%", (UnaryOperator<Number>) NumberMath::round}
    });

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        return false;
    }

    @Override
    public Number[] getValues(Event e) {
        return new Number[0];
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }
}
