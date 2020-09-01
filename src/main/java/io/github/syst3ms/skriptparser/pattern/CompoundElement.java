package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;

import java.util.Arrays;
import java.util.List;

/**
 * Multiple {@link PatternElement}s put together in order.
 */
public class CompoundElement implements PatternElement {
    private final List<PatternElement> elements;

    public CompoundElement(List<PatternElement> elements) {
        this.elements = elements;
    }

    /**
     * Only used for unit tests
     */
    public CompoundElement(PatternElement... elements) {
        this(Arrays.asList(elements));
    }

    public List<PatternElement> getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof CompoundElement)) {
            return false;
        } else {
            var elems = ((CompoundElement) obj).elements;
            return elements.size() == elems.size() && elements.equals(elems);
        }
    }

    @Override
    public int match(String s, int index, MatchContext context) {
        var i = index;
        for (var element : elements) {
            context.advanceInPattern();
            var m = element.match(s, i, context);
            if (m == -1) {
                return -1;
            }
            i = m;
        }
        if (context.getSource().isEmpty() && i < s.length() - 1)
            return -1;
        return i;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        for (var element : elements) {
            builder.append(element);
        }
        return builder.toString();
    }
}
