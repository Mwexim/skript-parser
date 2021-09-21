package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * A group of multiple choices, represented by {@linkplain ChoiceElement}s
 */
public class ChoiceGroup implements PatternElement {
    private final List<ChoiceElement> choices;

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
            var choiceElements = ((ChoiceGroup) obj).choices;
            return choices.equals(choiceElements);
        }
    }

    @Override
    public int match(String s, int index, MatchContext context) {
        for (var choice : choices) {
            var branch = context.branch(choice.getElement());
            var m = choice.getElement().match(s, index, branch);
            if (m != -1) {
                context.merge(branch);
                if (choice.getMark() != null)
                    context.addMark(choice.getMark());
                return m;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        var joiner = new StringJoiner("|", "(", ")");
        for (var choice : choices) {
            if (choice.getMark() != null) {
                joiner.add(choice.getMark() + ":" + choice.getElement().toString());
            } else {
                joiner.add(choice.getElement().toString());
            }
        }
        return joiner.toString();
    }
}
