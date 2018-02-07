package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;

import java.util.List;

public abstract class CodeSection implements Effect {
    protected List<Effect> items;

    private CodeSection() {}

    public CodeSection(FileSection section) {
        throw new UnsupportedOperationException("Can't create a CodeSection by itself, you must extend from it");
    }

    public List<Effect> getItems() {
        return items;
    }
}
