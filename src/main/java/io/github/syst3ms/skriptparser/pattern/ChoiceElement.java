package io.github.syst3ms.skriptparser.pattern;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An element of a choice group.
 * Consists of a {@link PatternElement} and a parse mark (defaults to 0)
 * @see ChoiceGroup
 */
public class ChoiceElement {
    private final PatternElement element;
    @Nullable
    private final String mark;

    public ChoiceElement(PatternElement element, @Nullable String mark) {
        this.element = element;
        this.mark = mark;
    }

    @Nullable
    public String getMark() {
        return mark;
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
            var other = (ChoiceElement) obj;
            return element.equals(other.element) && Objects.equals(mark, other.mark);
        }
    }
}
