package io.github.syst3ms.skriptparser.lang.properties;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A base class for expressions that contain general properties.
 * In English, one can express properties in many different ways:
 * <ul>
 *     <li>Mwexim's book</li>
 *     <li>the book of Mwexim</li>
 * </ul>
 * This utility class acknowledges how useful and common such "property expressions" are, and provides a simple way
 * to implement them.
 * The class also provides default implementations of {@link #init(Expression[], int, ParseContext)}
 * and {@link #getValues(TriggerContext)}. Their default functionality is specified below.
 *
 * @param <T> The returned type of this expression.
 * @param <O> The type of the owner of this expression.
 * @author Mwexim
 */
public abstract class PropertyExpression<T, O> implements Expression<T> {
    private Expression<O> owner;

    /**
     * This default {@code init()} implementation automatically properly sets the owner of this property,
     * which can be accessed using {@link #getOwner()}. If this implementation is overridden for one reason
     * or another, it must call {@link #setOwner(Expression)} properly.
     * @param expressions an array of expressions representing all the expressions that are being passed
     *                    to this syntax element.
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
     * A simple default method that will apply {@link #getProperty(Object)} on the {@link #owner} of this property.
     * @param ctx the event
     * @return the values of this property after applying the {@link #getProperty(Object)} function on the owner.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T[] getValues(TriggerContext ctx) {
        return (T[]) owner.stream(ctx).map(this::getProperty).filter(Objects::nonNull).toArray(Object[]::new);
    }

    /**
     * For each owner, this method will be ran individually to convert it to this particular property.
     * @param owner the owner
     * @return the property value
     */
    @Nullable
    public T getProperty(O owner) {
        throw new UnsupportedOperationException("Override #getProperty(O) if you are planning to use the default functionality.");
    }

    @Override
    public boolean isSingle() {
        return owner.isSingle();
    }

    public Expression<O> getOwner() {
        return owner;
    }

    public void setOwner(Expression<O> owner) {
        this.owner = owner;
    }

    /**
     * There are 2 kinds of possession:
     * <ul>
     *     <li><b>Genitive:</b>Mwexim's book</li>
     *     <li><b>Regular:</b>book of Mwexim</li>
     * </ul>
     * One may use this method to check if the pattern is in the genitive form.
     * @param matchedPattern the matched pattern of a property
     * @return whether this pattern is in the genitive form or not.
     */
    public static boolean isGenitive(int matchedPattern) {
        return matchedPattern == 0;
    }
}
