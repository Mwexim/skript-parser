package io.github.syst3ms.skriptparser.lang;

import java.util.Optional;

/**
 * An expression whose value is known at parse time
 * @param <T> the type of the literal
 */
public interface Literal<T> extends Expression<T> {
    T[] getValues();

    default Optional<? extends T> getSingle() {
        return getSingle(TriggerContext.DUMMY);
    }

    @Override
    default T[] getValues(TriggerContext ctx) {
        return getValues();
    }

    static boolean isLiteral(Expression<?> exp) {
        return exp instanceof Literal || exp instanceof VariableString && ((VariableString) exp).isSimple();
    }
}
