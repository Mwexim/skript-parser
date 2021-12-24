package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing info about an {@link Expression} syntax
 * @param <C> the {@link Expression} class
 * @param <T> the return type of the {@link Expression}
 */
public class ExpressionInfo<C, T> extends SyntaxInfo<C> {
    private final PatternType<T> returnType;

    public ExpressionInfo(SkriptAddon registerer, Class<C> c, Type<T> returnType, boolean isSingle, int priority, List<PatternElement> patterns) {
        this(registerer, c, returnType, isSingle, priority, patterns, new HashMap<>());
    }

    public ExpressionInfo(SkriptAddon registerer, Class<C> c, Type<T> returnType, boolean isSingle, int priority, List<PatternElement> patterns, Map<String, Object> data) {
        super(registerer, c, priority, patterns, data);
        this.returnType = new PatternType<>(returnType, isSingle);
    }

    public PatternType<T> getReturnType() {
        return returnType;
    }
}