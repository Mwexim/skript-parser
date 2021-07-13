package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        // Keywords - makes matching remarkably faster in almost all cases
        var toCheck = s.substring(index).toLowerCase();
        for (var keyword : simplify()) {
            if (!toCheck.contains(keyword.toLowerCase()))
                return -1;
        }

        var i = index;
        for (var element : elements) {
            var m = element.match(s, i, context);
            if (m == -1) {
                return -1;
            }
            i = m;
            context.advanceInPattern();
        }
        if (context.getSource().isEmpty() && i < s.length() - 1)
            return -1;
        return i;
    }

    @Override
    public List<String> simplify() {
        return elements.stream()
                .map(PatternElement::simplify)
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
