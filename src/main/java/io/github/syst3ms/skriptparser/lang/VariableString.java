package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A string that possibly contains expressions inside it, meaning that its value may be unknown at runtime
 */
public class VariableString implements Expression<String> {
    public static final Pattern R_LITERAL_CONTENT_PATTERN = Pattern.compile("^(.+?)\\((.+)\\)\\1$"); // It's actually rare to be able to use '.+' raw like this
    /**
	 * An array containing raw data for this {@link VariableString}.
	 * Contains {@link String} and {@link Expression} elements
	 */
    private Object[] data;
    private boolean simple;

    private VariableString(Object[] data) {
        this.data = data;
        this.simple = data.length == 1 && data[0] instanceof String;
    }

    /**
     * Creates a new instance of a VariableString.
     * @param s the text to create a new instance from, with its surrounding quotes
     * @return {@code null} if either:
     * <ul>
     *     <li>The argument isn't quoted correctly</li>
     *     <li>{@link #newInstance(String)} returned null, which can happen when the string literal is of the form
     *     {@code "..."}</li>
     *     <li>Something went very wrong when parsing a raw literal {@code R"possible delimiter(...)possible delimiter'}
     *     </li>
     * </ul>. Returns a new instance of a VariableString otherwise.
     */
    @Nullable
    public static VariableString newInstanceWithQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return newInstance(s.substring(1, s.length() - 1));
        } else if (s.startsWith("'") && s.endsWith("'") && StringUtils.nextSimpleCharacterIndex(s, 0) == s.length()) {
            return new VariableString(new String[]{
                s.substring(1, s.length() - 1).replace("\\'", "'")
            });
        } else if (s.startsWith("R\"") && s.endsWith("\"")) {
            String content = s.substring(2, s.length() - 1);
            Matcher m = R_LITERAL_CONTENT_PATTERN.matcher(content);
            if (m.matches()) {
                return new VariableString(new String[]{m.group(2)});
            } else {
                // REMIND error
            }
        }
        return null;
    }

    /**
     * Creates a new instance of a VariableString from the text inside a string literal.
     * @param s the content of the string literal, without quotes
     * @return a new instance of a VariableString, or {@code null} if there are unbalanced {@literal %} symbols
     */
    @Nullable
    public static VariableString newInstance(String s) {
        List<Object> data = new ArrayList<>(StringUtils.count(s, "%"));
        StringBuilder sb = new StringBuilder();
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '%') {
                if (i == charArray.length - 1) {
                    return null;
                }
                String content = StringUtils.getPercentContent(s, i + 1);
                if (content == null) {
                    return null;
                }
                String toParse = content.replaceAll("\\\\(.)", "$1");
                Expression<?> expression = SyntaxParser.parseExpression(toParse, SyntaxParser.OBJECT_PATTERN_TYPE);
                if (expression == null) {
                    return null;
                }
                if (sb.length() > 0) {
                    data.add(sb.toString());
                    sb.setLength(0);
                }
                data.add(expression);
                i += content.length() + 1;
            } else if (c == '\\') {
                if (i + 1 == charArray.length) {
                    return null;
                }
                sb.append(charArray[++i]);
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            data.add(sb.toString());
        }
        return new VariableString(data.toArray());
    }

    /**
     * @return whether this VariableString is actually constant and whose value can be known at parse time
     */
    public boolean isSimple() {
        return simple;
    }

    @Override
    public String[] getValues(TriggerContext e) {
        return new String[]{toString(e)};
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param e the event
     * @return this VariableString represented in the given event. The behaviour of passing {@code null} without
     * checking {@link #isSimple()} is unspecified.
     */
    public String toString(TriggerContext e) {
        if (simple)
            return (String) data[0];
        StringBuilder sb = new StringBuilder();
        for (Object o : data) {
            if (o instanceof Expression) {
                sb.append(TypeManager.toString(((Expression) o).getValues(e)));
            } else {
                sb.append(o);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString(@Nullable TriggerContext e, boolean debug) {
        if (simple)
            return "\"" + data[0] + "\"";
        StringBuilder sb = new StringBuilder("\"");
        for (Object o : data) {
            if (o instanceof Expression) {
                sb.append('%').append(((Expression) o).toString(e, debug)).append('%');
            } else {
                sb.append(o);
            }
        }
        return sb.append("\"").toString();
    }

    public String defaultVariableName() {
        if (simple)
            return (String) data[0];
        StringBuilder sb = new StringBuilder();
        for (Object o : data) {
            if (o instanceof String) {
                sb.append(o);
            } else {
                assert o instanceof Expression;
                ExpressionInfo<?, ?> exprInfo = SyntaxManager.getExpressionExact((Expression<?>) o);
                assert exprInfo != null;
                sb.append("<").append(exprInfo.getReturnType().toString()).append(">");
            }
        }
        return sb.toString();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}
