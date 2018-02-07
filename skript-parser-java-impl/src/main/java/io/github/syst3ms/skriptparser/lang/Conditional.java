package io.github.syst3ms.skriptparser.lang;

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
    public void execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Effect walk() {
        if (mode == ConditionalMode.ELSE) {
            return getFirst();
        }
        Boolean c = condition.getSingle();
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
    public String toString(boolean debug) {
        return mode.name().toLowerCase().replace('_', ' ') + " " + condition.toString(debug);
    }

    public enum ConditionalMode {
        IF,
        ELSE_IF,
        ELSE;

        public boolean hasIf() {
            return this == IF || this == ELSE_IF;
        }

        public boolean hasElse() {
            return this == ELSE_IF || this == ELSE;
        }
    }
}
