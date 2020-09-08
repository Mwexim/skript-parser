package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.Nullable;

/**
 * Check if a given expression is an instance of a given type.
 *
 * @name Is Instance Of
 * @type CONDITION
 * @pattern %objects% (is|are)[( not|n't)] an instance of %type%
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprInstanceOf extends ConditionalExpression {

    static {
        Parser.getMainRegistration().addExpression(
                CondExprInstanceOf.class,
                Boolean.class,
                true,
                "%objects% (is|are)[1:( not|n't)] an instance of %type%");
    }

    private Expression<Object> expr;
    private Expression<Type<?>> type;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = (Expression<Object>) expressions[0];
        type = (Expression<Type<?>>) expressions[1];
        setNegated(parseContext.getParseMark() == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        var t = TypeManager.getByClass(expr.getReturnType());
        if (t.isEmpty())
            t = TypeManager.getByClassExact(Object.class);
        var cls1 = t.orElseThrow(AssertionError::new).getTypeClass();
        var cls2 = type.getSingle(ctx);

        return isNegated() != cls2
                .filter(c -> c.getTypeClass().isAssignableFrom(cls1))
                .isPresent();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " is not " : " is ") + "an instance of " + type.toString(ctx, debug);
    }
}
