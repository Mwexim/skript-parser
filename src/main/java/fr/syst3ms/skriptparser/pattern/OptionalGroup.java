package fr.syst3ms.skriptparser.pattern;

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
}
