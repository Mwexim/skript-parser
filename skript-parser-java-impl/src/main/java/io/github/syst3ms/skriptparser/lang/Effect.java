package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Effect implements SyntaxElement {
    @Nullable
    protected CodeSection parent;
    @Nullable
    protected Effect next;

    public static boolean runAll(Effect start, Event event) {
        Effect item = start;
        try {
            while (item != null)
                item = item.walk(event);
            return true;
        } catch (StackOverflowError so) {
            System.err.println("The script repeated itself infinitely !");
            return false;
        } catch (Exception e) {
            System.err.println("An exception occured. Stack trace :");
            e.printStackTrace();
        }
        return false;
    }

    public abstract void execute(Event e);

    @Nullable
    public CodeSection getParent() {
        return parent;
    }

    public Effect setParent(CodeSection section) {
        this.parent = section;
        return this;
    }

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

    public Effect setNext(@Nullable Effect next) {
        this.next = next;
        return this;
    }

    @Nullable
    protected Effect walk(Event e) {
        execute(e);
        return getNext();
    }
}
