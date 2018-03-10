package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import org.jetbrains.annotations.NotNull;

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
