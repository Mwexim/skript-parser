package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.util.List;

public abstract class CodeSection extends Effect {
    protected List<Effect> items;
    private Effect first;

    private CodeSection() {}

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    public CodeSection(FileSection section) {
        items = ScriptLoader.loadItems(section);
        first = items.get(0);
    }

    public List<Effect> getItems() {
        return items;
    }

    protected final Effect getFirst() {
        return first == null ? getNext() : first;
    }
}
