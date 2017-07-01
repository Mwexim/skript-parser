package io.github.bensku.skript.pattern;

/**
 * Optional pattern element. That is, an element which
 * might have length of 0.
 * 
 * This is specialized version for optional elements which contian
 * only simple text.
 *
 */
public class OptionalStrElement extends StrElement {

    public OptionalStrElement(String str) {
        super(str);
    }

    @Override
    public int matches(String str, int start) {
        String potentialMatch = str.substring(start, elementStr.length());
        if (potentialMatch.equals(elementStr)) {
            return start + elementStr.length();
        }
        return start;
    }

}
