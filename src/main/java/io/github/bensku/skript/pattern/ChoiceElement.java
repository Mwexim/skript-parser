package io.github.bensku.skript.pattern;

import java.util.List;

/**
 * An element which represents multiple choices of an element.
 * One of them must be preset for this to match.
 *
 */
public class ChoiceElement implements PatternElement {
    
    private List<PatternElement> elements;
    
    public ChoiceElement(List<PatternElement> elements) {
        this.elements = elements;
    }
    
    @Override
    public int matches(String str, int start) {
        for (PatternElement element : elements) {
            int pos = element.matches(str, start);
            if (pos != -1) { // This element matches!
                return pos;
            }
        }
        
        // No element matches :(
        return -1;
    }
}
