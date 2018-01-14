package io.github.syst3ms.skriptparser.pattern;

public class OptionalGroup implements PatternElement {
    private PatternElement element;

    public OptionalGroup(PatternElement element) {
        this.element = element;
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof OptionalGroup && element.equals(((OptionalGroup) obj).element);
    }
}
