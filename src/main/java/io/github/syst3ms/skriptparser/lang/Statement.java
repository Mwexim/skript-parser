package io.github.syst3ms.skriptparser.lang;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The base class for any runnable line of code inside of a script.
 * @see CodeSection
 * @see Effect
 */
public abstract class Statement implements SyntaxElement {
    @Nullable
    protected CodeSection parent;
    @Nullable
    protected Statement next;

    /**
     * Runs all code starting at a given point sequentially
     * @param start the Statement the method should first run
     * @param context the context
     * @return {@code true} if the code ran normally, and {@code false} if any exception occurred
     */
    public static boolean runAll(Statement start, TriggerContext context) {
        Optional<? extends Statement> item = Optional.of(start);
        try {
            while (item.isPresent())
                item = item.flatMap(i -> i.walk(context));
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
     * Executes this Statement
     * @param ctx the event
     */
    public abstract boolean run(TriggerContext ctx);

    /**
     * @return the parent of this Statement
     */
    public Optional<? extends CodeSection> getParent() {
        return Optional.ofNullable(parent);
    }

    /**
     * Sets the parent {@link CodeSection} of this Statement
     * @param section the parent
     * @return this Statement
     */
    public Statement setParent(CodeSection section) {
        this.parent = section;
        return this;
    }

    /**
     * @return the Statement after this one in the file. If this Statement is the last item of the section, returns the item after
     *         said section. If this Statement is the very last item of a trigger, returns {@code null}
     */
    public final Optional<? extends Statement> getNext() {
        if (next != null) {
            return Optional.of(next);
        } else if (parent != null) {
            return parent.getNext();
        } else {
            return Optional.empty();
        }
    }

    /**
     * Sets the Statement that is placed after this Statement in the file
     * @param next the Statement that is following this one
     * @return this Statement
     */
    public Statement setNext(@Nullable Statement next) {
        this.next = next;
        return this;
    }

    /**
     * By default, runs {@link #run(TriggerContext)} ; returns {@link #getNext()} if it returns true, or {@code null} otherwise.
     * Note : if this method is overridden, then the implementation of {@linkplain #run(TriggerContext)} doesn't matter.
     * @param ctx the event
     * @return the next item to be ran, or {@code null} if this is the last item to be executed
     */
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        var proceed = run(ctx);
        if (proceed) {
            return getNext();
        } else if (parent != null) {
            return parent.getNext();
        } else {
            return Optional.empty();
        }
    }
}
