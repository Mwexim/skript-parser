package io.github.bensku.skript.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.bensku.skript.type.Type;

/**
 * Manages list of all available expressions.
 *
 */
public class ExpressionManager {
    
    /**
     * Maps return types of expressions to pattern data which will also contain
     * the expression on question.
     */
    private Map<Type<?>, List<PatternInfo>> typesToPatterns;
    
    public ExpressionManager() {
        typesToPatterns = new HashMap<>();
    }
    
    public List<PatternInfo> getAllPatterns(Type<?> type) {
        return typesToPatterns.get(type);
    }
}
