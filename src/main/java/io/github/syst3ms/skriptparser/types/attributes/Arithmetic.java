package io.github.syst3ms.skriptparser.types.attributes;

import io.github.syst3ms.skriptparser.types.Type;

/**
 * An interface describing arithmetic operations between two types
 * @param <A> the first type
 * @param <R> the second type
 */
public interface Arithmetic<A, R> extends Type.Attribute<A> {

    R difference(A first, A second);

    A add(A value, R difference);

    A subtract(A value, R difference);

    Class<? extends R> getRelativeType();
}
