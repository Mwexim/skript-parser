package fr.syst3ms.skriptparser;

import fr.syst3ms.skriptparser.pattern.ChoiceElement;
import fr.syst3ms.skriptparser.pattern.ChoiceGroup;
import fr.syst3ms.skriptparser.pattern.CompoundElement;
import fr.syst3ms.skriptparser.pattern.ExpressionElement;
import fr.syst3ms.skriptparser.pattern.PatternElement;
import fr.syst3ms.skriptparser.pattern.RegexGroup;
import fr.syst3ms.skriptparser.pattern.TextElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternParser {
    private static final Pattern PARSE_MARK_PATTERN = Pattern.compile("(\\d+?)¦.*");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(-)?([*~])?(?<types>[\\w\\/]+)(?:@(-?1))?");
    private static final StringBuilder EMPTY_STRING_BUILDER = new StringBuilder("");

    public static PatternElement parsePattern(String pattern) {
        List<PatternElement> elements = new ArrayList<>();
        StringBuilder textBuilder = EMPTY_STRING_BUILDER;
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '[') {
                String s = getEnclosedText(pattern, '[', ']', i);
                if (s == null) {
                    error("Unclosed optional group at index " + i);
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = EMPTY_STRING_BUILDER;
                }
                i += s.length() + 1; // sets i to the closing bracket, for loop does the rest
                PatternElement content = parsePattern(s);
                if (content == null) {
                    return null;
                }
                elements.add(content);
            } else if (c == '(') {
                String s = getEnclosedText(pattern, '(', ')', i);
                if (s == null) {
                    error("Unclosed choice group at index " + i);
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = EMPTY_STRING_BUILDER;
                }
                i += s.length() + 1;
                String[] choices = pattern.split("(?<!\\\\)|");
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
                String s = getEnclosedText(pattern, '<', '>', i);
                if (s == null) {
                    error("Unclosed regex group at index " + i);
                    return null;
                }
                if (textBuilder.length() != 0) {
                    elements.add(new TextElement(textBuilder.toString()));
                    textBuilder = EMPTY_STRING_BUILDER;
                }
                i += s.length() + 1;
                Pattern pat;
                try {
                    pat = Pattern.compile(s);
                } catch (PatternSyntaxException e) {
                    error("Invalid regex : '" + s + "'");
                    return null;
                }
                elements.add(new RegexGroup(pat));
            } else if (c == '%') {
                /*
                 * Can't use getEnclosedText as % acts for both opening and closing
                 * Moreover, there's no need of checking for nested stuff
                 */
                int nextIndex = pattern.indexOf('%', i + 1);
                if (nextIndex == -1) {
                    error("Unclosed variable declaration at index " + i);
                    return null;
                }
                String s = pattern.substring(i + 1, nextIndex);
                Matcher m = VARIABLE_PATTERN.matcher(s);
                if (!m.matches()) {
                    error("Invalid variable definition");
                    return null;
                } else {
                    boolean nullable = m.group(1) != null;
                    ExpressionElement.Acceptance acceptance = ExpressionElement.Acceptance.BOTH;
                    if (m.group(2) != null) {
                        String acc = m.group(2);
                        if (acc.equals("~")) {
                            acceptance = ExpressionElement.Acceptance.EXPRESSIONS_ONLY;
                        } else {
                            acceptance = ExpressionElement.Acceptance.LITERALS_ONLY;
                        }
                    }
                    int time = 0;
                    if (m.group(4) != null) {
                        String t = m.group(4);
                        time = t.length() == 2 ? -1 : 1;
                    }
                    String typeString = m.group("types");
                    String[] types = typeString.split("/");
                    // TODO convert to classes
                }
            } else if (c == '\\') {
                if (i == pattern.length() - 1) {
                    error("Backslash sequence at the end of the pattern");
                    return null;
                } else {
                    textBuilder.append(chars[++i]);
                }
            } else {
                textBuilder.append(c);
            }
        }
        if (elements.size() == 1) {
            return elements.get(0);
        } else {
            return new CompoundElement(elements);
        }
    }

    private static String getEnclosedText(String pattern, char opening, char closing, int start) {
        int n = 0;
        for (int i = start; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '\\') {
                i++;
            } else if (pattern.charAt(i) == closing) {
                if (n == 0) {
                    return pattern.substring(start + 1, i); // We don't want the beginning bracket in there
                }
                n--;
            } else if (pattern.charAt(i) == opening) {
                n++;
            }
        }
        return null;
    }
    private static void error(String error) {
        // TODO
    }
}
