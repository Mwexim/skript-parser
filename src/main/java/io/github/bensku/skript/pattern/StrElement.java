package io.github.bensku.skript.pattern;

/**
 * Static string element: contains text that will not change.
 *
 */
public class StrElement implements PatternElement {
    
    protected String elementStr;
    
    public StrElement(String str) {
        this.elementStr = str;
    }
    
    @Override
    public int matches(String str, int start) {
        String potentialMatch = str.substring(start, elementStr.length());
        if (potentialMatch.equals(elementStr)) {
            return start + elementStr.length();
        }
        return -1;
    }

}
