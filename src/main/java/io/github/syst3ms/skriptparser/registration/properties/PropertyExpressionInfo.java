package io.github.syst3ms.skriptparser.registration.properties;

import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.types.Type;

import java.util.List;

/**
 * A class containing info about a {@link PropertyExpression} syntax
 * @param <C> the {@link PropertyExpression} class
 * @param <T> the return type of the {@link PropertyExpression}
 */
public class PropertyExpressionInfo<C, T> extends ExpressionInfo<C, T> {
    private final String property;

    public PropertyExpressionInfo(Class<C> c, List<PatternElement> patterns, SkriptAddon registerer, Type<T> returnType, String property, int priority) {
        super(c, patterns, registerer, returnType, false, priority);
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}