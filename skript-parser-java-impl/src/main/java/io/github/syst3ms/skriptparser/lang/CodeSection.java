package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.util.List;

public abstract class CodeSection extends Effect {
    protected List<Effect> items;
    private Effect first;
    private Effect last;

    public void loadSection(FileSection section) {
        setTriggerItems(ScriptLoader.loadItems(section));
    }

    public final void setTriggerItems(List<Effect> items) {
        this.items = items;
        for (Effect item : items) {
            item.setParent(this);
        }
        first = items.get(0);
        last = items.get(items.size() - 1).setNext(getNext());
    }

    public List<Effect> getItems() {
        return items;
    }

    protected final Effect getFirst() {
        return first == null ? getNext() : first;
    }

    protected final Effect getLast() {
        return last == null ? getNext() : last;
    }
}
