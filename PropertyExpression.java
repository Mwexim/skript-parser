package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.events.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.function.Function;

/**
 * A base class for expressions that contain general properties.
 * In English, we can express properties in many different ways:
 * <ul>
 *     <li>Mwexim's book</li>
 *     <li>the book of Mwexim</li>
 * </ul>
 * This utility class will make sure you won't need to write multiple patterns each time
 * and ensures you can easily handle all these properties, using the {@link #setOwner(Expression)}
 * and {@link #getOwner()} methods to handle the owner.
 *
 * The class also has a built-in {@link #init(Expression[], int, ParseContext)} and {@link #getValues(TriggerContext)},
 * ensuring you only have to do the bare minimum. These methods are very basic though, so most of the time,
 * you'll want to override them anyway.
 *
 * @param <T> The returned type of this expression.
 * @param <O> The type of the owner of this expression.
 */
public abstract class PropertyExpression<T, O> implements Expression<T> {
    private Expression<O> owner;

    public Expression<O> getOwner() {
        return owner;
    }

    public void setOwner(Expression<O> owner) {
        this.owner = owner;
    }
    
    /**
     * If you're property only relies on one simple method, you can define that method here
     * using a {@link Function}. This function will be applied at the {@link #getValues(TriggerContext)} method
     * of this class.
     * @return the function that needs to be applied in order to get the correct values.
     */
    public Function<O[], T[]> getPropertyFunction() {
        return null;
    }

    /**
     * There are 2 kinds of possession:
     * <ul>
     *     <li><b>Genitive:</b>Mwexim's book</li>
     *     <li><b>Regular:</b>book of Mwexim</li>
     * </ul>
     * Because both have different patterns and the order of the expressions can be important,
     * you can use this method to check if the pattern is the Genitive form. If it's not, it's the regular form.
     * @param matchedPattern the matched pattern of a property
     * @return whether this pattern was the genitive form (true) or the regular form (false)
     */
    public static boolean isGenitive(int matchedPattern) {
        return matchedPattern == 0;
    }

    /**
     * This default {@code init()} implementation automatically properly sets the owner of this property,
     * which can be accessed using {@link #getOwner()}. If this implementation is overridden for one reason
     * or another, it must call {@link #setOwner(Expression<O>)} properly.
     * Most of the time, you'll still want to override this, because the only thing it does is
     * getting the first expression (because there is only one) and changing the {@link #owner} field
     * accordingly.
     * @param expressions an array of expressions representing all the expressions that are being passed
     *                    to this syntax element. As opposed to Buzzle, elements of this array can't be {@code null}.
     * @param matchedPattern the index of the pattern that was successfully matched. It corresponds to the order of
     *                       the syntaxes in registration
     * @param parseContext an object containing additional information about the parsing of this syntax element, like
     *                    regex matches and parse marks
     * @return whether the initialization was successful or not.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        setOwner((Expression<O>) expressions[0]);
        return true;
    }

    /**
     * A simple default method that will apply your basic function on the {@link #owner} of this property.
     * It checks for nullity and returns the appropriate values.
     *
     * @param ctx the event
     * @return the values of this property after applying the {@link #getPropertyFunction()} function on the owner.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T[] getValues(TriggerContext ctx) {
        O[] objs = getOwner().getValues(ctx);
        if (objs.length == 0) return (T[]) new Object[0];
        if (getPropertyFunction() == null)
            throw new UnsupportedOperationException("If you do not wish to override #getPropertyFunction(), you should always override #getValues()");
            
        return getPropertyFunction().apply(objs);
    }
}
