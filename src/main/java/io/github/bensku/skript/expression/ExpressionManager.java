package io.github.bensku.skript.expression;

import java.util.ArrayList;
import java.util.Comparator;
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
    
    private List<PatternInfo> patternList;
    
    public ExpressionManager() {
        typesToPatterns = new HashMap<>();
    }
    
    public List<PatternInfo> getAllPatterns(Type<?> type) {
        return typesToPatterns.get(type);
    }
    
    public List<PatternInfo> getAllPatterns() {
        return patternList;
    }
    
    public void registerPattern(PatternInfo pattern) {
        List<PatternInfo> typePatterns = typesToPatterns.getOrDefault(pattern.getExpression().getReturnType(), new ArrayList<>());
        typePatterns.add(pattern);
        
        patternList.add(pattern);
    }
    
    /**
     * Re-sorts patterns based on their priorities. Most used pattern
     * will tested first after this is done.
     */
    public void sortPatterns() {
        for (List<PatternInfo> typeList : typesToPatterns.values()) {
            typeList.sort(Comparator.naturalOrder());
        }
        patternList.sort(Comparator.naturalOrder());
    }
}
