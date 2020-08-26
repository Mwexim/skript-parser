package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.registration.tags.NormalTag;
import io.github.syst3ms.skriptparser.registration.tags.SkriptTags;
import io.github.syst3ms.skriptparser.registration.tags.Tag;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A string that possibly contains expressions inside it, meaning that its value may be unknown at parse time
 */
@SuppressWarnings("ConfusingArgumentToVarargsMethod")
public class VariableString implements Expression<String> {
    public static final Pattern R_LITERAL_CONTENT_PATTERN = Pattern.compile("^(.+?)\\((.+)\\)\\1$"); // It's actually rare to be able to use '.+' raw like this
    /**
     * An array containing raw data for this {@link VariableString}.
     * Contains {@link String} and {@link Expression} elements
     */
    private final Object[] data;
    private final boolean simple;

    private VariableString(Object[] data) {
        this.data = data;
        this.simple = data.length == 1 && data[0] instanceof String;
    }


    /**
     * Creates a new instance of a VariableString.
     * @param s the text to create a new instance from, with its surrounding quotes
     * @param logger the logger
     * @return {@code null} if either:
     * <ul>
     *     <li>The argument isn't quoted correctly</li>
     *     <li>{@link #newInstance(String, ParserState, SkriptLogger)} returned null, which can happen when the string literal is of the form
     *     {@code "..."}</li>
     *     <li>Something went very wrong when parsing a raw literal {@code R"possible delimiter(...)possible delimiter'}
     *     </li>
     * </ul>. Returns a new instance of a VariableString otherwise.
     */
    public static Optional<VariableString> newInstanceWithQuotes(String s, ParserState parserState, SkriptLogger logger) {
        if (s.startsWith("\"") && s.endsWith("\"") && StringUtils.nextSimpleCharacterIndex(s, 0) == s.length()) {
            return newInstance(s.substring(1, s.length() - 1), parserState, logger);
        } else if (s.startsWith("'") && s.endsWith("'") && StringUtils.nextSimpleCharacterIndex(s, 0) == s.length()) {
            return Optional.of(new VariableString(new String[]{
                    s.substring(1, s.length() - 1).replace("\\'", "'")
            }));
        } else if (s.startsWith("R\"") && s.endsWith("\"")) {
            var content = s.substring(2, s.length() - 1);
            var m = R_LITERAL_CONTENT_PATTERN.matcher(content);
            if (m.matches()) {
                return Optional.of(new VariableString(new String[]{m.group(2)}));
            } else {
                logger.error("Invalid R literal string", ErrorType.MALFORMED_INPUT);
            }
        }
        return Optional.empty();
    }

    /**
     * Creates a new instance of a VariableString from the text inside a string literal.
     * @param s the content of the string literal, without quotes
     * @param parserState the current parser state
     * @param logger the logger
     * @return a new instance of a VariableString, or {@code null} if there are unbalanced {@literal %} symbols
     */
    public static Optional<VariableString> newInstance(String s, ParserState parserState, SkriptLogger logger) {
        List<Object> data = new ArrayList<>(StringUtils.count(s, "%"));
        var sb = new StringBuilder();
        var charArray = s.toCharArray();
        for (var i = 0; i < charArray.length; i++) {
            var c = charArray[i];
            if (c == '%') {
                if (i == charArray.length - 1) {
                    return Optional.empty();
                }
                var content = StringUtils.getPercentContent(s, i + 1);
                var toParse = content.map(co -> co.replaceAll("\\\\(.)", "$1"));
                if (toParse.isEmpty())
                    return Optional.empty();
                logger.recurse();
                var expression = SyntaxParser.parseExpression(toParse.get(), SyntaxParser.OBJECTS_PATTERN_TYPE, parserState, logger);
                logger.callback();
                if (expression.isEmpty())
                    return Optional.empty();
                if (sb.length() > 0) {
                    data.add(sb.toString());
                    sb.setLength(0);
                }
                data.add(expression.get());
                i += content.get().length() + 1;
            } else if (c == '<') {
                if (i == charArray.length - 1) {
                    return Optional.empty();
                }
                var content = StringUtils.getBracketContent(s, i + 1, '>');
                if (content.isEmpty()) {
                    sb.append(c);
                    continue;
                }
                logger.recurse();
                var tag = SkriptTags.parseTag('<' + content.get() + '>', logger);
                logger.callback();
                if (tag.isEmpty()) {
                    return Optional.empty();
                }
                if (sb.length() > 0) {
                    data.add(sb.toString());
                    sb.setLength(0);
                }
                data.add(tag.get());
                i += content.get().length() + 1;
            } else if (c == '\\') {
                if (i + 1 == charArray.length) {
                    return Optional.empty();
                }
                sb.append(charArray[++i]);
            } else if (c == '&') {
                logger.recurse();
                var tag = SkriptTags.parseTag("" + c + charArray[++i], logger);
                logger.callback();
                if (tag.isEmpty()) {
                    return Optional.empty();
                }
                if (sb.length() > 0) {
                    data.add(sb.toString());
                    sb.setLength(0);
                }
                data.add(tag.get());
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            data.add(sb.toString());
        }
        return Optional.of(new VariableString(data.toArray()));
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getValues(TriggerContext ctx) {
        return new String[]{toString(ctx)};
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    /**
     * @param ctx the event
     * @return this VariableString represented in the given event. The behaviour of passing {@code null} without
     * checking {@link #isSimple()} is unspecified.
     */
    public String toString(TriggerContext ctx) {
        if (simple)
            return (String) data[0];

        boolean occasional = SkriptTags.isOccasionalEnabled();
        int resets = 1;
        var sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            var o = data[i];
            if (o instanceof Expression) {
                sb.append(TypeManager.toString(((Expression<?>) o).getValues(ctx)));
            } else if (o instanceof Tag) {
                if (((Tag) o).isOccasional() && !occasional)
                    continue;
                if (o.equals(SkriptTags.RESET_TAG)) {
                    resets++;
                    continue;
                }
                Tag tag = appendTag((Tag) o, ctx, occasional, ++i, data);
                System.out.println(((NormalTag) tag).getParameter());
                sb.append(tag.getValue());
                int resetIndex = CollectionUtils.indexOfNth(Arrays.asList(data), SkriptTags.RESET_TAG, resets++);
                if (resetIndex == -1) {
                    break;
                }
                i = resetIndex;
            } else {
                sb.append(o);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        if (simple)
            return "\"" + data[0] + "\"";
        var sb = new StringBuilder("\"");
        for (var o : data) {
            if (o instanceof Expression) {
                sb.append('%').append(((Expression<?>) o).toString(ctx, debug)).append('%');
            } else if (o instanceof Tag) {
                sb.append(o.toString());
            } else {
                sb.append(o);
            }
        }
        return sb.append("\"").toString();
    }

    /**
     * @return whether this VariableString is actually constant and whose value can be known at parse time
     */
    public boolean isSimple() {
        return simple;
    }

    public String defaultVariableName() {
        if (simple)
            return (String) data[0];
        var sb = new StringBuilder();
        for (var o : data) {
            if (o instanceof String) {
                sb.append(o);
            } else if (o instanceof Tag) {
                sb.append(o.toString());
            } else {
                assert o instanceof Expression;
                Optional<? extends ExpressionInfo<? extends Expression<?>, ?>> exprInfo = SyntaxManager.getExpressionExact((Expression<?>) o);
                assert exprInfo.isPresent();
                sb.append("<").append(exprInfo.get().getReturnType().toString()).append(">");
            }
        }
        return sb.toString();
    }

    private Tag appendTag(Tag tag, TriggerContext ctx, boolean occasional, int n, Object[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = n; i < data.length; i++) {
            Object o = data[i];
            if (o instanceof Expression) {
                sb.append(TypeManager.toString(((Expression<?>) o).getValues(ctx)));
            } else if (o instanceof Tag) {
                if (((Tag) o).isOccasional() && !occasional)
                    continue;
                if (o.equals(SkriptTags.RESET_TAG)) {
                    sb.append(SkriptTags.RESET_TAG.getValue());
                    tag.setAffected(sb.toString());
                    return tag;
                }
                sb.append(appendTag((Tag) o, ctx, occasional, ++i, data).getValue());
            } else {
                sb.append(o);
            }
        }
        tag.setAffected(sb.toString());
        return tag;
    }
}
