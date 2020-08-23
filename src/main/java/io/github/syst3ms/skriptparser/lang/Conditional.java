package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A {@linkplain CodeSection code section} representing a condition. It can either be :
 * <ul>
 *     <li>An "if" condition</li>
 *     <li>An "else if" condition</li>
 *     <li>An "else" condition, that does not include any condition</li>
 * </ul>
 * This "mode" is described by a {@link ConditionalMode} value, accessible through {@link #getMode()}.
 * This is the only syntax element to be understood natively by the parser.
 * @see ConditionalMode
 */
public class Conditional extends CodeSection {
    private final ConditionalMode mode;
    @Nullable
    private final Expression<Boolean> condition;
    private Conditional fallingClause;

    public Conditional(FileSection section, @Nullable Expression<Boolean> condition, ConditionalMode mode, ParserState parserState, SkriptLogger logger) {
        super.loadSection(section, parserState, logger);
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
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        assert condition != null || mode == ConditionalMode.ELSE;
        if (mode == ConditionalMode.ELSE) {
            return getFirst();
        }
        Optional<? extends Boolean> c = condition.getSingle(ctx);
        if (c.filter(b -> b).isPresent()) {
            return getFirst();
        } else if (fallingClause != null) {
            return Optional.of(fallingClause);
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
            fallingClause.setParent(this);
        }
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException(); // This is never actually called, and should never be
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return mode + (condition != null ? " " + condition.toString(ctx, debug) : "");
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
