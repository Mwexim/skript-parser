package io.github.bensku.skript;

import java.util.ArrayList;
import java.util.List;

import io.github.bensku.skript.expression.PatternInfo;

/**
 * Represents a parser state which is given to pattern elements when they are
 * parsed. Elements may use it to inject another expressions
 *
 */
public class ParserState {
    
    /**
     * Expressions with patterns that are found inside this state.
     */
    private List<PatternInfo> expressions;
    private int mask;
    
    public ParserState(int expectedExpressions) {
        expressions = new ArrayList<>(expectedExpressions);
    }
    
    public void mask(int xor) {
        mask ^= xor;
    }
    
    public void addExpression(PatternInfo info) {
        expressions.add(info);
    }
}
