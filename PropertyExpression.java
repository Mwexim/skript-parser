package com.github.mwexim.buzzle.lang.base;

import com.github.mwexim.buzzle.lang.Expression;

/**
 * A base class for expressions that contain general properties.
 * In English, we can express properties in many different ways:
 * <ul>
 *     <li>Mwexim's book</li>
 *     <li>the book of Mwexim</li>
 * </ul>
 * This utility class will make sure you won't need to write multiple patterns each time
 * and ensures you can easily handle all these properties, using
 */
public abstract class PropertyExpression<T, O> implements Expression<T> {
    private Expression<O> owner;

    public Expression<O> getOwner() {
        return owner;
    }

    public void setOwner(Expression<O> owner) {
        this.owner = owner;
    }



}
