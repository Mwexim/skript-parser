package io.github.bensku.skript.expression;

import java.util.List;

import io.github.bensku.skript.pattern.PatternElement;
import io.github.bensku.skript.type.Type;

/**
 * Represents something that returns something and has patterns which
 * may result it being matched.
 *
 */
public class Expression<T> {
    
    private List<PatternElement> patterns;
    private Type<T> returnType;
    
    public Expression(List<PatternElement> patterns, Type<T> returnType) {
        this.patterns = patterns;
        this.returnType = returnType;
    }
    
    public List<PatternElement> getPatterns() {
        return patterns;
    }
    
    public Type<T> getReturnType() {
        return returnType;
    }
}
