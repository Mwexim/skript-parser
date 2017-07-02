package io.github.bensku.skript.expression;

import java.util.List;

import io.github.bensku.skript.pattern.PatternElement;

/**
 * Represents something that returns something and has patterns which
 * may result it being matched.
 *
 */
public class Expression {
    
    private List<PatternElement> patterns;
    
    /**
     * Represents some of the standard expression priority values.
     *
     */
    public static class Priority {
        
        /**
         * Language features (function calls) are ALWAYS parsed first.
         * Use this only when absolutely necessary.
         */
        public static final int LANG_FEATURE = 0;
        
        public static final int HIGH = 1;
        
        public static final int MEDIUM = 2;
        
        public static final int LOW = 3;
    }
}
