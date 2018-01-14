package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.classes.SkriptParser;

import java.util.Arrays;
import java.util.List;

/**
 * Multiple {@link PatternElement}s put together in order.
 */
public class CompoundElement implements PatternElement {
    private List<PatternElement> elements;

    public CompoundElement(List<PatternElement> elements) {
        this.elements = elements;
    }

    /**
     * Only used for unit tests
     */
    public CompoundElement(PatternElement... elements) {
        this(Arrays.asList(elements));
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CompoundElement)) {
            return false;
        } else {
            List<PatternElement> elems = ((CompoundElement) obj).elements;
            return elements.size() == elems.size() && elements.equals(elems);
        }
    }

	@Override
	public int match(String s, int index, SkriptParser parser) {
        int i = index;
        for (PatternElement element : elements) {
            int m = element.match(s, i + 1, parser);
            if (m == -1) {
                return -1;
            }
            i = m;
        }
        return i;
    }
}
