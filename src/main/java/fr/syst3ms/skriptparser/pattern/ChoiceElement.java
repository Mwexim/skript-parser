package fr.syst3ms.skriptparser.pattern;

public class ChoiceElement {
    private PatternElement element;
    private int parseMark;

    public ChoiceElement(PatternElement element, int parseMark) {
        this.element = element;
        this.parseMark = parseMark;
    }
}
