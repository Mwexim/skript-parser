package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Conditional extends CodeSection {
    private ConditionalMode mode;
    @Nullable
    private Expression<Boolean> condition;
    private Conditional fallingClause;

    public Conditional(FileSection section, @Nullable Expression<Boolean> condition, ConditionalMode mode) {
        super.loadSection(section);
        this.condition = condition;
        this.mode = mode;
    }

    public ConditionalMode getMode() {
        return mode;
    }

    @Override
    protected Effect walk(Event e) {
        assert condition != null || mode == ConditionalMode.ELSE;
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
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return mode + (condition != null ? " " + condition.toString(e, debug) : "");
    }

    public enum ConditionalMode {
        IF, ELSE_IF, ELSE;

        private final String[] modeNames = {"if", "else if", "else"};

        @Override
        public String toString() {
            return modeNames[this.ordinal()];
        }
    }
}
