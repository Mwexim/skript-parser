package io.github.bensku.skript;

import java.util.ArrayList;
import java.util.List;

import io.github.bensku.skript.expression.ExpressionManager;
import io.github.bensku.skript.pattern.ChoiceElement;
import io.github.bensku.skript.pattern.MultiElement;
import io.github.bensku.skript.pattern.PatternElement;
import io.github.bensku.skript.pattern.VariableElement;
import io.github.bensku.skript.type.Type;
import io.github.bensku.skript.type.TypeManager;

/**
 * Parses Skript's special patters into pattern elements.
 *
 */
public class PatternParser {
    
    private ExpressionManager exprManager;
    private TypeManager typeManager;
    
    public PatternParser(ExpressionManager exprManager, TypeManager typeManager) {
        this.exprManager = exprManager;
        this.typeManager = typeManager;
    }
    
    public List<PatternElement> parse(String pattern) {
        List<PatternElement> elements = new ArrayList<>();
        
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            // Check for special characters
            int notClosed = 1;
            boolean malformed = true; // If pattern has an error
            
            switch (c) {
            case '(': // Choice element
                for (int j = i; j < chars.length; j++) {
                    int c2 = chars[j];
                    if (c2 == '(') {
                        notClosed++;
                    } else if (c2 == ')') {
                        notClosed--;
                    }
                    
                    if (notClosed == 0) {
                        malformed = false;
                        elements.add(parseChoice(pattern.substring(i + 1, j)));
                        i = j;
                        break;
                    }
                }
                
                if (malformed) {
                    throw new InvalidPatternException("Invalid choice element starting at " + i);
                }
                break;
            case '[': // Optional element
                for (int j = i; j < chars.length; j++) {
                    int c2 = chars[j];
                    if (c2 == '[') {
                        notClosed++;
                    } else if (c2 == ']') {
                        notClosed--;
                    }
                    
                    if (notClosed == 0) {
                        malformed = false;
                        elements.addAll(parse(pattern.substring(i + 1, j)));
                        i = j;
                        break;
                    }
                }
                
                if (malformed) {
                    throw new  InvalidPatternException("Invalid optional element starting at " + i);
                }
                break;
            case '%': // Variable element
                int end = pattern.indexOf('%', i + 1);
                
                String[] typeNames = pattern.substring(i + 1, end).split("/");
                List<Type<?>> types = new ArrayList<>(typeNames.length);
                for (int s = 0; s < typeNames.length; s++) {
                    types.add(typeManager.getForName(typeNames[s]));
                }
                
                elements.add(new VariableElement(types, exprManager));
                i = end;
                break;
            }
        }
        
        return elements;
    }
    
    public PatternElement parseChoice(String pattern) {
        // TODO: choices inside choices?
        String[] parts = pattern.split(" ");
        List<PatternElement> choices = new ArrayList<>();
        for (String choice : parts) {
            choices.add(new MultiElement(parse(choice)));
        }
        
        return new ChoiceElement(choices);
    }
}
