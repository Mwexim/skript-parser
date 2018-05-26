package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;

/**
 * An object used to retrieve depending on which pattern was matched
 * @param <T> the type of the data to be retrieved
 * @see io.github.syst3ms.skriptparser.lang.SyntaxElement#init(Expression[], int, ParseResult)
 */
public class PatternInfos<T> {
    private String[] patterns;
    private Object[] data;

    public PatternInfos(Object[][] infos) {
        patterns = new String[infos.length];
        data = new Object[infos.length];
        for (int i = 0; i < infos.length; i++) {
            Object[] info = infos[i];
            if (info.length != 2 || !(info[0] instanceof String))
                throw new SkriptParserException("Arrays inside of PatternInfos must be of the form {String, T}");
            patterns[i] = (String) info[0];
            data[i] = info[1];
        }
    }

    @SuppressWarnings("unchecked")
    public T getInfo(int pattern) {
        return (T) data[pattern];
    }

    public String[] getPatterns() {
        return patterns;
    }
}
