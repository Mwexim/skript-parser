package io.github.bensku.skript.expression;

import io.github.bensku.skript.pattern.PatternElement;

/**
 * Contains a pattern and expressions for which it belongs to.
 *
 */
public class PatternInfo implements Comparable<PatternInfo> {
    
    private Expression<?> expr;
    private int patternIndex;
    
    private PatternElement pattern;
    
    private int priority;
    
    public PatternInfo(Expression<?> expr, int index, PatternElement pattern) {
        this.expr = expr;
        this.patternIndex = index;
        this.pattern = pattern;
    }
    
    public Expression<?> getExpression() {
        return expr;
    }
    
    public int getIndex() {
        return patternIndex;
    }
    
    public PatternElement getPattern() {
        return pattern;
    }

    @Override
    public int compareTo(PatternInfo o) {
        return Integer.compare(priority, o.priority);
    }
}
