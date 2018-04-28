package io.github.syst3ms.skriptparser.types.changers;

public interface Arithmetic<A, R> {

    R difference(A first, A second);

    A add(A value, R difference);

    A subtract(A value, R difference);

    Class<? extends R> getRelativeType();
}
