package io.github.bensku.skript.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages list of all available expressions.
 *
 */
public class ExpressionManager {
    
    /**
     * Maps return types of expressions to pattern data which will also contain
     * the expression on question.
     */
    private Map<Class<?>, List<PatternInfo>> typesToPatterns;
    
    public ExpressionManager() {
        typesToPatterns = new HashMap<>();
    }
    
    public List<PatternInfo> getAllPatterns(Class<?> returnType) {
        return typesToPatterns.get(returnType);
    }
}
