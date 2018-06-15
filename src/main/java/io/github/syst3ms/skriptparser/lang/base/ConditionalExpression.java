package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;

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
 * Other, "non-conditional" boolean expressions should implement {@link Expression<Boolean>}
 * @see io.github.syst3ms.skriptparser.expressions.ExprWhether
 * @see io.github.syst3ms.skriptparser.lang.Conditional
 * @see io.github.syst3ms.skriptparser.lang.While
 */
public abstract class ConditionalExpression implements Expression<Boolean> {
    private boolean negated = false;

    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    @Override
    public Boolean[] getValues(TriggerContext e) {
        return new Boolean[]{check(e)};
    }

    protected abstract boolean check(TriggerContext e);
}
