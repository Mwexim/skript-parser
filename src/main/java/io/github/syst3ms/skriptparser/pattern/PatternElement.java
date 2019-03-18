package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The superclass of all elements of a pattern.
 */
public interface PatternElement {

    /**
     * Attempts to match the {@link PatternElement} to a string at a specified index.
     * About the index, make sure to never increment the index by some arbitrary value when returning
     *
     * @param s the string to match this PatternElement against
     * @param index the index of the string at which this PatternElement should be matched
     * @return the index at which the matching should continue afterwards if successful. Otherwise, {@literal -1}
     */
    int match(String s, int index, MatchContext parser);

    static List<PatternElement> flatten(PatternElement element) {
        if (element instanceof CompoundElement) {
            return ((CompoundElement) element).getElements();
        } else {
            return Collections.singletonList(element);
        }
    }

    static List<PatternElement> getPossibleInputs(List<PatternElement> elements) {
        List<PatternElement> possibilities = new ArrayList<>();
        for (PatternElement element : elements) {
            if (element instanceof TextElement || element instanceof RegexGroup) {
                if (element instanceof TextElement) {
                    String text = ((TextElement) element).getText();
                    if (text.isEmpty() || text.matches("\\s*") && elements.size() == 1) {
                        return possibilities;
                    } else if (text.matches("\\s*")) {
                        continue;
                    }
                }
                possibilities.add(element);
                return possibilities;
            } else if (element instanceof ChoiceGroup) {
                for (ChoiceElement choice : ((ChoiceGroup) element).getChoices()) {
                    List<PatternElement> possibleInputs = getPossibleInputs(flatten(choice.getElement()));
                    possibilities.addAll(possibleInputs);
                }
                return possibilities;
            } else if (element instanceof ExpressionElement) {
                possibilities.add(element);
                return possibilities;
            } else if (element instanceof OptionalGroup) {
                possibilities.addAll(getPossibleInputs(flatten(((OptionalGroup) element).getElement())));
            }
        }
        possibilities.add(new TextElement("\0"));
        return possibilities;
    }
}
