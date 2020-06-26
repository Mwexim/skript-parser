package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;

import java.util.List;

/**
 * A class containing info about an {@link Expression} syntax
 * @param <C> the {@link Expression} class
 * @param <T> the return type of the {@link Expression}
 */
public class ExpressionInfo<C, T> extends SyntaxInfo<C> {
    private final PatternType<T> returnType;

    public ExpressionInfo(Class<C> c, List<PatternElement> patterns, SkriptAddon registerer, Type<T> returnType, boolean isSingle, int priority) {
        super(c, patterns, priority, registerer);
        this.returnType = new PatternType<>(returnType, isSingle);
    }

    public PatternType<T> getReturnType() {
        return returnType;
    }
}