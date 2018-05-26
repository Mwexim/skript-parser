package io.github.syst3ms.skriptparser.pattern;

/**
 * An element of a choice group.
 * Consists of a {@link PatternElement} and a parse mark (defaults to 0)
 */
public class ChoiceElement {
    private PatternElement element;
    private int parseMark;

    public ChoiceElement(PatternElement element, int parseMark) {
        this.element = element;
        this.parseMark = parseMark;
    }

    public int getParseMark() {
        return parseMark;
    }

    public PatternElement getElement() {
        return element;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ChoiceElement)) {
            return false;
        } else {
            ChoiceElement other = (ChoiceElement) obj;
            return element.equals(other.element) && parseMark == other.parseMark;
        }
    }
}
