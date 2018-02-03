package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;

public interface Expression<T> extends SyntaxElement {
    T[] getValues();

    default boolean change(ChangeMode changeMode) {
        return false;
    }

    default T getSingle() {
        T[] values = getValues();
        if (values.length == 0) {
            return null;
        } else if (values.length > 1) {
            throw new SkriptRuntimeException("Can't call getSingle on an expression that returns multiple values !");
        } else {
            return values[0];
        }
    }
}
