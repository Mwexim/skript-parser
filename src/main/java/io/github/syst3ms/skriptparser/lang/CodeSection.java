package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * It is important to note that Conditional is the only section to be understood
 * natively by the parser, meaning it won't go through the process of syntax parsing.
 * @see Conditional
 * @see Loop
 * @see io.github.syst3ms.skriptparser.lang.base.ConditionalExpression
 */
public abstract class CodeSection extends Statement {
    protected List<Statement> items;
    private Statement first;
    private Statement last;

    /**
     * This methods determines the logic of what is being done to the elements inside of this section.
     * By default, this simply parses all items inside it, but this can be overridden.
     * In case an extending class just needs to do some additional operations on top of what the default implementation
     * already does, then call {@code super.loadSection(section)} before any such operations.
     * @param section the {@link FileSection} representing this {@linkplain CodeSection}
     * @param logger
     */
    public void loadSection(FileSection section, SkriptLogger logger) {
        setItems(ScriptLoader.loadItems(section, logger));
    }

    @Override
    @Contract("_ -> fail")
    public boolean run(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected abstract Statement walk(TriggerContext ctx);

    /**
     * Sets the items inside this lists, and also modifies other fields, reflected through the outputs of {@link #getFirst()},
     * {@link #getLast()} and {@link Statement#getParent()}.
     * @param items the items to set
     */
    public final void setItems(List<Statement> items) {
        this.items = items;
        for (Statement item : items) {
            item.setParent(this);
        }
        first = items.isEmpty() ? null : items.get(0);
        last = items.isEmpty() ? null : items.get(items.size() - 1).setNext(getNext());
    }

    /**
     * The items returned by this method are not representative of the execution of the code, meaning that all items
     * in the list may not be all executed. The list should rather be considered as a flat view of all the lines inside the
     * section. Prefer {@link Statement#runAll(Statement, TriggerContext)} to run the contents of this section
     * @return all items inside this section
     */
    public List<Statement> getItems() {
        return items;
    }

    /**
     * @return the first item of this section, or the item after the section if it's empty, or {@code null} if there is
     * no item after this section, in the latter case
     */
    @Nullable
    protected final Statement getFirst() {
        return first == null ? getNext() : first;
    }

    /**
     * @return the last item of this section, or the item after the section if it's empty, or {@code null} if there is
     * no item after this section, in the latter case
     */
    @Nullable
    protected final Statement getLast() {
        return last == null ? getNext() : last;
    }
}
