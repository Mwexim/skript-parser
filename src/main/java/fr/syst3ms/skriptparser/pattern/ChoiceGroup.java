package fr.syst3ms.skriptparser.pattern;

import java.util.List;

public class ChoiceGroup implements PatternElement {
    private List<ChoiceElement> choices;

    public ChoiceGroup(List<ChoiceElement> choices) {
        this.choices = choices;
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }
}
