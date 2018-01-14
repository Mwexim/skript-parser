package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.classes.SkriptParser;

import java.util.Arrays;
import java.util.List;

/**
 * A group of multiple choices, consisting of multiple {@link ChoiceElement}
 */
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
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChoiceGroup)) {
            return false;
        } else {
            List<ChoiceElement> choiceElements = ((ChoiceGroup) obj).choices;
            return choices.size() == choiceElements.size() && choices.equals(choiceElements);
        }
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        for (ChoiceElement choice : choices) {
            int m = choice.getElement().match(s, index + 1, parser);
            if (m != -1) {
                parser.addMark(choice.getParseMark());
                return m;
            }
        }
        return -1;
    }
}
