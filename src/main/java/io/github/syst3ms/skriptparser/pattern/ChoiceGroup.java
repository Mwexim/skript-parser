package io.github.syst3ms.skriptparser.pattern;

import java.util.Arrays;
import java.util.List;

public class ChoiceGroup implements PatternElement {
    private List<ChoiceElement> choices;

    public ChoiceGroup(List<ChoiceElement> choices) {
        this.choices = choices;
    }

    /**
     * Only used in unit tests
     */
    public ChoiceGroup(ChoiceElement... choices) {
        this(Arrays.asList(choices));
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChoiceGroup)) {
            return false;
        } else {
            List<ChoiceElement> choiceElements = ((ChoiceGroup) obj).choices;
            return choices.size() == choiceElements.size() && choices.equals(choiceElements);
        }
    }
}
