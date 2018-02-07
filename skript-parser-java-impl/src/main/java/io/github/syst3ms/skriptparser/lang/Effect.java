package io.github.syst3ms.skriptparser.lang;

public abstract class Effect implements SyntaxElement {
    protected CodeSection parent;
    protected Effect next;

    public abstract void execute();

    public CodeSection getParent() {
        return parent;
    }

    public Effect setParent(CodeSection section) {
        this.parent = section;
        return this;
    }

    public final Effect getNext() {
        return next == null ? parent.getNext() : next;
    }

    public Effect setNext(Effect next) {
        this.next = next;
        return this;
    }

    protected Effect walk() {
        execute();
        if (next != null) {
            return next;
        } else {
            return parent.getNext();
        }
    }
}
