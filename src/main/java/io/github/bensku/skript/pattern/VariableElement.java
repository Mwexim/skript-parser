package io.github.bensku.skript.pattern;

import java.util.List;
import java.util.Set;

import io.github.bensku.skript.expression.ExpressionManager;

/**
 * Represents a variable in text.
 *
 */
public class VariableElement implements PatternElement {
    
    private List<Class<?>> returnTypes;
    private ExpressionManager exprManager;
    
    public VariableElement(List<Class<?>> returnTypes, ExpressionManager exprManager) {
        this.returnTypes = returnTypes;
        this.exprManager = exprManager;
    }
    
    @Override
    public int matches(String str, int start) {
        for (Class<?> type : returnTypes) {
            Set<PatternElement> elements = exprManager.getAllPatterns(type);
            for (PatternElement element : elements) {
                int pos = element.matches(str, start);
                if (pos != -1) { // Found a match!
                    return pos;
                }
            }
        }
        
        // Nothing matches...
        return -1;
    }

}
