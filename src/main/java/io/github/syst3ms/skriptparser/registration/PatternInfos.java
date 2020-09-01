package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;

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
}
