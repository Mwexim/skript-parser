package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.SkriptParser;

/**
 * A group containing an optional {@link PatternElement}, that can be omitted
 */
public class OptionalGroup implements PatternElement {
    private PatternElement element;

    public OptionalGroup(PatternElement element) {
        this.element = element;
    }

    public PatternElement getElement() {
        return element;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof OptionalGroup)) {
            return false;
        } else {
            OptionalGroup other = (OptionalGroup) obj;
            return element.equals(other.element);
        }
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        if (parser.getOriginalElement().equals(this))
            parser.advanceInPattern();
        int m = element.match(s, index, parser);
        return m != -1 ? m : index;
    }

    @Override
    public String toString() {
        return "[" + element.toString() + "]";
    }
}
