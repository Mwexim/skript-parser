package io.github.syst3ms.skriptparser.lang.interfaces;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;

/**
 * An interface used to mark that an {@link Expression <Boolean>} can't be used as-is,
 * and that it must be used in a condition or inside {@code whether %boolean%}.
 *
 * Other boolean expressions should implement {@link Expression<Boolean>}
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
    public Boolean[] getValues(Event e) {
        return new Boolean[]{check(e)};
    }

    protected abstract boolean check(Event e);
}
