package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.pattern.ChoiceGroup;

/**
 * An object used to retrieve depending on which pattern was matched
 * @param <T> the type of the data to be retrieved
 * @see SyntaxElement#init(Expression[], int, ParseContext)
 */
public class PatternInfos<T> {
    private final String[] patterns;
    private final Object[] data;

    public PatternInfos(Object[][] infos) {
        patterns = new String[infos.length];
        data = new Object[infos.length];
        for (var i = 0; i < infos.length; i++) {
            var info = infos[i];
            if (info.length != 2 || !(info[0] instanceof String))
                throw new SkriptParserException("Arrays inside of PatternInfos must be of the form {String, T}");
            patterns[i] = (String) info[0];
            data[i] = info[1];
        }
    }

    /**
     * The data corresponding to the pattern index
     * @param pattern the pattern index
     * @return the corresponding data
     */
    @SuppressWarnings("unchecked")
    public T getInfo(int pattern) {
        return (T) data[pattern];
    }

    /**
     * @return a list of all patterns
     */
    public String[] getPatterns() {
        return patterns;
    }

    /**
     * Joins all the possibilities together, forming a {@link ChoiceGroup}
     * with numeric parse marks.
     * @return a single choice group pattern
     */
    public String toChoiceGroup() {
        var builder = new StringBuilder("(");
        for (int i = 0; i < patterns.length; i++)
            builder.append(i).append(':').append(patterns[i]).append('|');
        return builder.append(')').toString();
    }
}
