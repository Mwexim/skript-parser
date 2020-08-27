package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.PropertyExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Length of a string.
 *
 * @name Length
 * @pattern length of %string%
 * @pattern %string%'s length
 * @since ALPHA
 * @author Romitou
 */
public class ExprLength extends PropertyExpression<Number, String> {

    static {
        Main.getMainRegistration().addPropertyExpression(
                ExprLength.class,
                Number.class,
                true,
                "string",
                "length"
        );
    }

    @Override
    public Optional<? extends Function<? super String[], ? extends Number[]>> getPropertyFunction() {
        return Optional.of(strings -> new Number[]{strings[0].length()});
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "length of " + getOwner().toString(ctx, debug);
    }
}
