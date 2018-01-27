package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.SimpleFileLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class CodeSection implements Effect {
    protected List<Effect> items;

    public CodeSection(List<Effect> items) {
        this.items = items;
    }

    public List<Effect> getItems() {
        return items;
    }
}
