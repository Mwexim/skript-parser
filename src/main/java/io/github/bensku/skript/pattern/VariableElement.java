package io.github.bensku.skript.pattern;

import java.util.List;
import java.util.Set;

import io.github.bensku.skript.ParserState;
import io.github.bensku.skript.expression.ExpressionManager;
import io.github.bensku.skript.expression.PatternInfo;
import io.github.bensku.skript.type.Type;

/**
 * Represents a variable in text.
 *
 */
public class VariableElement implements PatternElement {
    
    private List<Type<?>> returnTypes;
    private ExpressionManager exprManager;
    
    public VariableElement(List<Type<?>> types, ExpressionManager exprManager) {
        this.returnTypes = types;
        this.exprManager = exprManager;
    }
    
    @Override
    public int matches(String str, int start, ParserState state) {
        for (Type<?> type : returnTypes) {
            List<PatternInfo> patterns = exprManager.getAllPatterns(type);
            for (PatternInfo info : patterns) {
                int pos = info.getPattern().matches(str, start);
                if (pos != -1) { // Found a match!
                    state.addExpression(info); // This data will be needed runtime
                    return pos;
                }
            }
        }
        
        // Nothing matches...
        return -1;
    }

    @Override
    public int matches(String str, int start) {
        throw new UnsupportedOperationException(); // This shouldn't be called here, ever
    }

}
