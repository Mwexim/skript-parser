package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;

/**
 * Multiple {@link PatternElement}s put together in order, denominated as a group.
 */
public class NamedGroup extends CompoundElement {
    private final PatternElement element;
    private final String name;

    public NamedGroup(PatternElement element, String name) {
        this.element = element;
        this.name = name;
    }

    public PatternElement getElement() {
        return element;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof NamedGroup)) {
            return false;
        } else {
            NamedGroup group = (NamedGroup) object;
            return name.equals(group.name) && element.equals(group.element);
        }
    }

    @Override
    public int match(String s, int index, MatchContext context) {
        var i = index;
        i = element.match(s, i, context);
        if (i != -1)
            context.addNamedGroup(name, s.substring(index, i));
        System.out.println(s + " :: (" + index + ", " + i + ") :: " + s.substring(i));
        for (var el : ((CompoundElement) element).getElements())
            System.out.println(el.getClass() + " :: " + el);
        return i;
    }

    @Override
    public String toString() {
        return "{'" + name + "':" + element + "}";
    }
}
