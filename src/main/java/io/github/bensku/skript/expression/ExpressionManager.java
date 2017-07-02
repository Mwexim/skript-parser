package io.github.bensku.skript.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.github.bensku.skript.pattern.PatternElement;

/**
 * Manages list of all available expressions.
 *
 */
public class ExpressionManager {
    
    /**
     * Maps return types of expressions to pattern data which will also contain
     * the expression on question.
     */
    private Map<Class<?>, Set<PatternElement>> typesToPatterns;
    
    public ExpressionManager() {
        typesToPatterns = new HashMap<>();
    }
    
    public Set<PatternElement> getAllPatterns(Class<?> returnType) {
        return typesToPatterns.get(returnType);
    }
}
