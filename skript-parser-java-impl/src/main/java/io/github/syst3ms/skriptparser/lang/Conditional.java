package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.file.FileSection;

public class Conditional extends CodeSection {
    private ConditionalMode mode;
    private Expression<Boolean> condition;
    private Conditional fallingClause;

    public Conditional(FileSection section, Expression<Boolean> condition, ConditionalMode mode) {
        super(section);
        this.condition = condition;
        this.mode = mode;
    }

    public ConditionalMode getMode() {
        return mode;
    }

    @Override
    public void execute(Event e) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Effect walk(Event e) {
        if (mode == ConditionalMode.ELSE) {
            return getFirst();
        }
        Boolean c = condition.getSingle(e);
        if (c != null && c) {
            return getFirst();
        } else if (fallingClause != null){
            return fallingClause.getFirst();
        } else {
            return getNext();
        }
    }

    public void setFallingClause(Conditional conditional) {
        this.fallingClause = conditional;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return mode.name().toLowerCase().replace('_', ' ') + " " + condition.toString(e, debug);
    }

    public enum ConditionalMode { IF, ELSE_IF, ELSE }
}
