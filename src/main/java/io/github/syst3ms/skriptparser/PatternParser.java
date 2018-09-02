package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.pattern.ChoiceElement;
import io.github.syst3ms.skriptparser.pattern.ChoiceGroup;
import io.github.syst3ms.skriptparser.pattern.CompoundElement;
import io.github.syst3ms.skriptparser.pattern.ExpressionElement;
import io.github.syst3ms.skriptparser.pattern.OptionalGroup;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.RegexGroup;
import io.github.syst3ms.skriptparser.pattern.TextElement;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternParser {
    private static final Pattern PARSE_MARK_PATTERN = Pattern.compile("(\\d+?)\\xa6.*");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(-)?([*~])?(=)?(?<types>[\\w/]+)?");

    /**
     * Parses a pattern and returns a {@link PatternElement}. This method can be called by itself, for example when parsing group constructs.
     * @param pattern the pattern to be parsed
     * @return the parsed PatternElement, or {@literal null} if something went wrong.
     */
    @Nullable
    public PatternElement parsePattern(String pattern) {
        List<PatternElement> elements = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '[') {
                String s = StringUtils.getEnclosedText(pattern, '[', ']', i);
                if (s == null) {
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.length() + 1; // sets i to the closing bracket, for loop does the rest
                Matcher m = PARSE_MARK_PATTERN.matcher(s);
                PatternElement content;
                if (m.matches()) {
                    String mark = m.group(1);
                    int markNumber = Integer.parseInt(mark);
                    String rest = s.substring(mark.length() + 1, s.length());
                    PatternElement e = parsePattern(rest);
                    if (e == null) {
                        return null;
                    }
                    content = new ChoiceGroup(Collections.singletonList(new ChoiceElement(e, markNumber))); // I said I would keep the other constructor for unit tests
                } else {
                    content = parsePattern(s);
                    if (content == null) {
                        return null;
                    }
                }
                elements.add(new OptionalGroup(content));
            } else if (c == '(') {
                String s = StringUtils.getEnclosedText(pattern, '(', ')', i);
                if (s == null) {
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.length() + 1;
                String[] choices = StringUtils.splitVerticalBars(s);
                List<ChoiceElement> choiceElements = new ArrayList<>();
                for (String choice : choices) {
                    Matcher matcher = PARSE_MARK_PATTERN.matcher(choice);
                    if (matcher.matches()) {
                        String mark = matcher.group(1);
                        int markNumber = Integer.parseInt(mark);
                        String rest = choice.substring(mark.length() + 1, choice.length());
                        PatternElement choiceContent = parsePattern(rest);
                        if (choiceContent == null) {
                            return null;
                        }
                        choiceElements.add(new ChoiceElement(choiceContent, markNumber));
                    } else {
                        PatternElement choiceContent = parsePattern(choice);
                        if (choiceContent == null) {
                            return null;
                        }
                        choiceElements.add(new ChoiceElement(choiceContent, 0));
                    }
                }
                elements.add(new ChoiceGroup(choiceElements));
            } else if (c == '<') {
                String s = StringUtils.getEnclosedText(pattern, '<', '>', i);
                if (s == null) {
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = new StringBuilder();
                }
                i += s.length() + 1;
                Pattern pat;
                try {
                    pat = Pattern.compile(s);
                } catch (PatternSyntaxException e) {
                    return null;
                }
                elements.add(new RegexGroup(pat));
            } else if (c == '%') {
                /*
                 * Can't use getEnclosedText as % acts for both opening and closing,
                 * and there's no need of checking for nested stuff
                 */
                int nextIndex = pattern.indexOf('%', i + 1);
                if (nextIndex == -1) {
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder.setLength(0);
                }
                String s = pattern.substring(i + 1, nextIndex);
                i = nextIndex;
                Matcher m = VARIABLE_PATTERN.matcher(s);
                if (!m.matches()) {
                    return null;
                } else {
                    boolean nullable = m.group(1) != null;
                    ExpressionElement.Acceptance acceptance = ExpressionElement.Acceptance.ALL;
                    if (m.group(2) != null) {
                        String acc = m.group(2);
                        if (acc.equals("~")) {
                            acceptance = ExpressionElement.Acceptance.EXPRESSIONS_ONLY;
                        } else {
                            acceptance = ExpressionElement.Acceptance.LITERALS_ONLY;
                        }
                    }
                    String typeString = m.group("types");
                    String[] types = typeString.split("/");
                    List<PatternType<?>> patternTypes = new ArrayList<>();
                    for (String type : types) {
                        PatternType<?> t = TypeManager.getPatternType(type);
                        if (t == null) {
                            return null;
                        }
                        patternTypes.add(t);
                    }
                    boolean acceptConditional = m.group(3) != null;
                    if (acceptConditional && patternTypes.stream().noneMatch(t -> t.getType().getTypeClass() == Boolean.class))
                        throw new SkriptParserException("Can't use the '=' flag on non-boolean types");
                    elements.add(new ExpressionElement(patternTypes, acceptance, nullable, acceptConditional));
                }
            } else if (c == '\\') {
                if (i == pattern.length() - 1) {
                    return null;
                } else {
                    textBuilder.append(chars[++i]);
                }
            } else if (c == '|') {
                String[] groups = StringUtils.splitVerticalBars(pattern);
                List<ChoiceElement> choices = new ArrayList<>();
                for (String choice : groups) {
                    Matcher matcher = PARSE_MARK_PATTERN.matcher(choice);
                    if (matcher.matches()) {
                        String mark = matcher.group(1);
                        int markNumber = Integer.parseInt(mark);
                        String rest = choice.substring(mark.length() + 1, choice.length());
                        PatternElement choiceContent = parsePattern(rest);
                        if (choiceContent == null) {
                            return null;
                        }
                        choices.add(new ChoiceElement(choiceContent, markNumber));
                    } else {
                        PatternElement choiceContent = parsePattern(choice);
                        if (choiceContent == null) {
                            return null;
                        }
                        choices.add(new ChoiceElement(choiceContent, 0));
                    }
                }
                elements.add(new ChoiceGroup(choices));
            } else if (c == ']' || c == ')' || c == '>') { // Closing brackets are skipped over, so this marks an error
                return null;
            } else {
                textBuilder.append(c);
            }
        }
        if (textBuilder.length() != 0) {
            elements.add(new TextElement(textBuilder.toString()));
            textBuilder.setLength(0);
        }
        if (elements.size() == 1) {
            return elements.get(0);
        } else {
            return new CompoundElement(elements);
        }
    }

}
