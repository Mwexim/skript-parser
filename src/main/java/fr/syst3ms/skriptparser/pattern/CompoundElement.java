package fr.syst3ms.skriptparser.pattern;

import java.util.List;

public class CompoundElement implements PatternElement {
    private List<PatternElement> elements;

    public CompoundElement(List<PatternElement> elements) {
        this.elements = elements;
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }
}
