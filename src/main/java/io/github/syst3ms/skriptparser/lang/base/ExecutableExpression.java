package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.expressions.ExecExprListOperators;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * A base class for syntax that can be used as {@link Expression} or {@link Effect}.
 * An example of this could be:
 * <ul>
 *     {@code replace all {x} in {y} with {z}} <br>
 *     {@code print "%replace all {x} in {y} with {z}%}
 * </ul>
 * Both syntaxes are valid and this enables productive and better coding practices.
 * <br>
 * The behaviour is different based on how the syntax is used:
 * <ul>
 *     <li>If the syntax is used like an expression, the result should not change any expressions
 *     (like variables) whatsoever.</li>
 *     <li>Otherwise, if the syntax is used like an effect, the result should be used to perform
 *     actions according to the syntax.</li>
 * </ul>
 * To look at an example, like {@link ExecExprListOperators}:
 * <ul>
 *     {@code set {x} to pop {y::*}} should set the variable {@code x} to the last element of the list {@code y}. <br>
 *     {@code pop {y::*}} should remove the last element of the list {@code y}, because it is used as an effect now.
 * </ul>
 */
public abstract class ExecutableExpression<T> extends Effect implements Expression<T> {

	@Override
    protected void execute(TriggerContext ctx) {
        getValues(ctx, true);
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        return getValues(ctx, false);
    }

    /**
     * Retrieves all values of this expression, if used as one.
     * Otherwise, if used as an effect, performs side-effects with certain
     * {@link ExecutableExpression behaviour}. Note that when this is not the case,
     * this syntax, by convention, should not have any side-effects.
     * @param ctx the context
     * @param isEffect whether this syntax is used as effect or as an expression
     * @return an array of the values
     */
    public abstract T[] getValues(TriggerContext ctx, boolean isEffect);
}
