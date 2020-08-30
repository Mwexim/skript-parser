package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.expressions.ExprWhether;
import io.github.syst3ms.skriptparser.lang.Conditional;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.sections.SecWhile;

/**
 * A base class for all boolean expressions (i.e {@linkplain Expression<Boolean>}) that should be treated as
 * "conditional". This means that they can't be used "as-is" (e.g in trying to set a variable to them).
 * Instead, they can only be used either in :
 * <ol>
 *     <li>Conditions ({@literal if} and {@literal else if})</li>
 *     <li>While loops</li>
 *     <li>Expression whose boolean parameters are declared as {@literal %=boolean%} inside their syntax.</li>
 * </ol>
 * The simplest way to use a value of a conditional expression as if it were a regular boolean expression is to use
 * the {@code whether %=boolean%} expression.
 *
 * Other, non-conditional boolean expressions should implement {@link Expression<Boolean>}
 * @see ExprWhether
 * @see Conditional
 * @see SecWhile
 */
public abstract class ConditionalExpression implements Expression<Boolean> {
    private boolean negated = false;

    /**
     * Whether a condition is negated. This is used in conjunction with {@link #setNegated(boolean)}.
     * @return whether the condition is negated
     */
    public boolean isNegated() {
        return negated;
    }


    /**
     * Decides whether the output of a condition should be inverted in order to create a "negated" condition.
     * This was made a built-in method because it is a very common feature of conditions.
     * @param negated whether the condition should be negated
     */
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    @Override
    public Boolean[] getValues(TriggerContext ctx) {
        return new Boolean[]{check(ctx)};
    }

    protected abstract boolean check(TriggerContext ctx);
}
