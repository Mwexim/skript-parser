package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Nullable;

/**
 * A {@linkplain CodeSection code section} representing a condition. It can either be :
 * <ul>
 *     <li>An "if" condition</li>
 *     <li>An "else if" condition</li>
 *     <li>An "else" condition, that does not include any condition</li>
 * </ul>
 * This "mode" is described by a {@link ConditionalMode} value, accessible through {@link #getMode()}.
 * @see ConditionalMode
 */
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

    /**
     * @return the {@link ConditionalMode} describing this Conditional
     * @see ConditionalMode
     */
    public ConditionalMode getMode() {
        return mode;
    }

    @Override
    protected Effect walk(TriggerContext e) {
        assert condition != null || mode == ConditionalMode.ELSE;
        if (mode == ConditionalMode.ELSE) {
            return getFirst();
        }
        Boolean c = condition.getSingle(e);
        if (c != null && c) {
            return getFirst();
        } else if (fallingClause != null){
            return fallingClause;
        } else {
            return getNext();
        }
    }

    /**
     * @param conditional the Conditional object this Conditional falls back to when it's condition verifies to
     *                    false. Setting this to an "if" Conditional may cause unexpected/confusing behaviour.
     */
    public void setFallingClause(Conditional conditional) {
        if (fallingClause != null) {
            fallingClause.setFallingClause(conditional);
        } else {
            fallingClause = conditional;
        }
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public String toString(@Nullable TriggerContext e, boolean debug) {
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
