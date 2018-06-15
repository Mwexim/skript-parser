package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a section of runnable code. This parser guarantees the existence of {@link Conditional}, {@link Loop} and
 * {@link While}.<br>
 * <br>
 * It is important to note that Conditional is the sole section to be understood
 * natively by the parser, meaning it won't go through the process of syntax parsing.
 * @see Conditional
 * @see Loop
 * @see While
 * @see io.github.syst3ms.skriptparser.lang.base.ConditionalExpression
 */
public abstract class CodeSection extends Effect {
    protected List<Effect> items;
    private Effect first;
    private Effect last;

    /**
     * This methods determines the logic of what is being done to the elements inside of this section.
     * By default, this simply parses all items inside it, but this can be overriden.
     * In case an extending class just needs to do some additional operations on top of what the default implementation
     * already does, then call {@code super.loadSection(section)} before any such operations.
     * @param section the {@link FileSection} representing this {@linkplain CodeSection}
     */
    public void loadSection(FileSection section) {
        setItems(ScriptLoader.loadItems(section));
    }

    @Override
    @Contract("_ -> fail")
    public void execute(TriggerContext e) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected abstract Effect walk(TriggerContext e);

    /**
     * Sets the items inside this lists, and also modifies other fields, reflected through {@link #getFirst()},
     * {@link #getLast()} and {@link Effect#getParent()}.
     * @param items the items to set
     */
    public final void setItems(List<Effect> items) {
        this.items = items;
        for (Effect item : items) {
            item.setParent(this);
        }
        first = items.get(0);
        last = items.get(items.size() - 1).setNext(getNext());
    }

    /**
     * The items returned by this method are not representative of the execution of the code, meaning that all items
     * in the list may not be all executed. The list should rather be considered as a flat view of all the items in the
     * section. For actually running them, use {@link Effect#runAll(Effect, TriggerContext)}
     * @return all items inside this section
     */
    public List<Effect> getItems() {
        return items;
    }

    /**
     * @return the first item of this section, or the item after the section if it's empty, or {@code null} if there is
     * no item after this section, in the latter case
     */
    @Nullable
    protected final Effect getFirst() {
        return first == null ? getNext() : first;
    }

    /**
     * @return the last item of this section, or the item after the section if it's empty, or {@code null} if there is
     * no item after this section, in the latter case
     */
    @Nullable
    protected final Effect getLast() {
        return last == null ? getNext() : last;
    }
}
