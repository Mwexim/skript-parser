package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class CodeSection extends Effect {
    protected List<Effect> items;
    private Effect first;
    private Effect last;

    public void loadSection(FileSection section) {
        setTriggerItems(ScriptLoader.loadItems(section));
    }

    @Override
    @Contract("_ -> fail")
    public void execute(Event e) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected abstract Effect walk(Event e);

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

    @Nullable
    protected final Effect getFirst() {
        return first == null ? getNext() : first;
    }

    @Nullable
    protected final Effect getLast() {
        return last == null ? getNext() : last;
    }
}
