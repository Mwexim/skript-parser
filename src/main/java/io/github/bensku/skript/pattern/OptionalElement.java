package io.github.bensku.skript.pattern;

import java.util.List;

/**
 * Optional pattern element. That is, an element which
 * might have length of 0.
 *
 */
public class OptionalElement implements PatternElement {
    
    private List<PatternElement> elements;
    
    public OptionalElement(List<PatternElement> elements) {
        this.elements = elements;
    }
    
    @Override
    public int matches(String str, int start) {
        // Loop all elements inside this optional element
        // If one fails, we'll just figure out that this optional par
        int pos = start;
        for (PatternElement element : elements) {
            pos = element.matches(str, pos);
            if (pos == -1) { // One failed to match, this optional part is NOT present
                return start;
            }
        }
        return pos; // This part matched, completely
    }

}
