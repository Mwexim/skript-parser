package io.github.bensku.skript.pattern;

import java.util.List;

/**
 * Just matches multiple elements.
 *
 */
public class MultiElement implements PatternElement {

    private List<PatternElement> elements;
    
    public MultiElement(List<PatternElement> elements) {
        this.elements = elements;
    }
    
    @Override
    public int matches(String str, int start) {
        int pos = start;
        for (PatternElement element : elements) {
            pos = element.matches(str, pos);
            if (pos == -1) { // One failed to match
                return -1;
            }
        }
        return pos; // This part matched, completely
    }

}
