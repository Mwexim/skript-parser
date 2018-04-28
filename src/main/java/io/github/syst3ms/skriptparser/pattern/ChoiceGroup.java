package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.SkriptParser;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

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

    public List<ChoiceElement> getChoices() {
        return choices;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ChoiceGroup)) {
            return false;
        } else {
            List<ChoiceElement> choiceElements = ((ChoiceGroup) obj).choices;
            return choices.size() == choiceElements.size() && choices.equals(choiceElements);
        }
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        if (parser.getOriginalElement().equals(this))
            parser.advanceInPattern();
        for (ChoiceElement choice : choices) {
            int m = choice.getElement().match(s, index, parser);
            if (m != -1) {
                parser.addMark(choice.getParseMark());
                return m;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("|", "(", ")");
        for (ChoiceElement choice : choices) {
            if (choice.getParseMark() != 0) {
                joiner.add(choice.getParseMark() + "\u00a6" + choice.getElement().toString());
            } else {
                joiner.add(choice.getElement().toString());
            }
        }
        return joiner.toString();
    }
}
