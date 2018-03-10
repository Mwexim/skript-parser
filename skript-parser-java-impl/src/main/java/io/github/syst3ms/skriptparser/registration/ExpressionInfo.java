package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents something that returns something and has PATTERNS which
 * may result it being matched
 */
public class ExpressionInfo<C, T> extends SyntaxInfo<C> {
    private PatternType<T> returnType;

    public ExpressionInfo(Class<C> c, List<PatternElement> patterns, Type<T> returnType, boolean isSingle, int priority) {
        super(c, patterns, priority);
        this.returnType = new PatternType<>(returnType, isSingle);
    }

    public PatternType<T> getReturnType() {
        return returnType;
    }
}