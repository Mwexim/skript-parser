package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import org.jetbrains.annotations.Nullable;

/**
 * The base class for any runnable line of code inside of a script.
 * @see CodeSection
 */
public abstract class Effect implements SyntaxElement {
    @Nullable
    protected CodeSection parent;
    @Nullable
    protected Effect next;

    /**
     * Runs all code starting at a given point sequentially
     * @param start the Effect the method should first run
     * @param context the context
     * @return {@code true} if the code ran normally, and {@code false} if any exception occurred
     */
    public static boolean runAll(Effect start, TriggerContext context) {
        Effect item = start;
        try {
            while (item != null)
                item = item.walk(context);
            return true;
        } catch (StackOverflowError so) {
            System.err.println("The script repeated itself infinitely !");
            return false;
        } catch (Exception e) {
            System.err.println("An exception occurred. Stack trace :");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Executes this Effect
     * @param e the event
     */
    public abstract void execute(TriggerContext e);

    /**
     * @return the parent of this Effect
     */
    @Nullable
    public CodeSection getParent() {
        return parent;
    }

    /**
     * Sets the parent {@link CodeSection} of this Effect
     * @param section the parent
     * @return this Effect
     */
    public Effect setParent(CodeSection section) {
        this.parent = section;
        return this;
    }

    /**
     * @return the Effect after this one in the file. If this is the last effect in the section, returns the item after
     *         the section. If this effect is the very last item of a trigger, returns {@code null}
     */
    @Nullable
    public final Effect getNext() {
        if (next != null) {
            return next;
        } else if (parent != null) {
            return parent.getNext();
        } else {
            return null;
        }
    }

    /**
     * Sets the Effect that is placed after this Effect in the file
     * @param next the Effect that is following this one
     * @return this Effect
     */
    public Effect setNext(@Nullable Effect next) {
        this.next = next;
        return this;
    }

    /**
     * By default, runs {@link #execute(TriggerContext)} and returns {@link #getNext()}. If this method is overriden in extending
     * classes, then the implementation of {@link #execute(TriggerContext)} doesn't matter.
     * @param e the event
     * @return the next item to be ran, or {@code null} if this is the last item to be executed
     */
    @Nullable
    protected Effect walk(TriggerContext e) {
        execute(e);
        return getNext();
    }
}
