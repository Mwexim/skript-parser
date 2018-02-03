package io.github.syst3ms.skriptparser.lang;

import java.util.List;

public abstract class CodeSection implements Effect {
    protected List<Effect> items;

    public CodeSection(List<Effect> items) {
        this.items = items;
    }

    public List<Effect> getItems() {
        return items;
    }
}
